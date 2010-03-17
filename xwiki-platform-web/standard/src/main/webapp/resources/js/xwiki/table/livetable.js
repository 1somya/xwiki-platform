(function(){

/**
 * XWiki namespace
 */
if (typeof XWiki == "undefined") {
    XWiki = new Object();
}

/**
 * widgets namespace
 */
if (typeof XWiki.widgets == "undefined") {
    XWiki.widgets = new Object();
}

/**
  * The class representing an AJAX-populated live table.
  * It is (almost) independent of the underlying HTML markup, a function passed as an argument being
  * responsible with displaying the content corresponding to a row. Uses JSON for the response
  * encoding.
  */
XWiki.widgets.LiveTable = Class.create({
  /**
    * @param url The base address for requesting the table data.
    * @param domNodeName The node supposed to hold the data rows, should be a <tbody>. DOM element or
    *   identifier.
    * @param handler A javascript function called for displaying fetched rows. The function
    *   accepts a JSON-parsed object and returns a DOM <tr> node
    * @param options An object with options for the live table. Supported options:
    * <ul>
    * <li>"limit" the maximum number of row entries in the table</li>
    * <li>"maxPages" the maximum number of pages to display at the same time in the pagination section.</li>
    * </ul>
    * @todo Make this a valid ARIA table: http://www.w3.org/TR/aria-role/#structural
    */
  initialize: function(url, domNodeName, handler, options)
  {
    if (!options) {
      var options = {};
    }
     
    // id of the root element that encloses this livetable
    this.domNodeName = domNodeName;

    // Remove, if present, the message that indicates the table cannot execute.
    // (It can since we are executing JavaScript).
    if ($(this.domNodeName).down('tr.xwiki-livetable-initial-message')) {
	  $(this.domNodeName).down('tr.xwiki-livetable-initial-message').remove();
    }
    
    // id of the display element (the inner dynamic table) of this livetable
    // defined by convention as the root node id on which is appenned "-display".
    // fallback on the unique "display1" id for backward compatibility.
    this.displayNode = $(domNodeName + "-display") || $('display1');

    // Node under which all forms controls (input, selects, etc.) will be filters for this table
    this.filtersNode = options.filtersNode || $(domNodeName).down(".xwiki-livetable-display-filters");

    // Array of nodes under which pagination for this livetable will be displayed.
    this.paginationNodes = options.paginationNodes || $(this.domNodeName).select(".xwiki-livetable-pagination");

    if (typeof options == "undefined") {
       options = {};
    }

    this.limit = options.limit || 10;
    this.action = options.action || "view"; // FIXME check if this can be removed safely.

    this.permalinks = options.permalinks || true;

    // Initialize pagination
    if (typeof this.paginationNodes != "undefined") {
       this.paginator = new LiveTablePagination(this, this.paginationNodes, options.maxPages || 10);
    }
    // Initialize filters
    if (this.filtersNode) {
      var initialFilters = this.permalinks ? this.getFiltersFromHash() : new Object();
      this.filter = new LiveTableFilter(this, this.filtersNode, initialFilters);
    }

    if ($(domNodeName + "-tagcloud"))
    {
       this.tagCloud = new LiveTableTagCloud(this, domNodeName + "-tagcloud");
    }
    this.loadingStatus = $(this.domNodeName + '-ajax-loader') || $('ajax-loader');
    this.limitsDisplay = $(this.domNodeName + '-limits') || new Element("div");
    this.filters = "";
    this.handler = handler || function(){};
    this.totalRows = -1;
    this.fetchedRows = new Array();
    this.getUrl = url;
    this.lastoffset = 1;
    this.sendReqNo = 0;
    this.recvReqNo = 0;

    this.observeSortableColumns();   
 
    var initialPage = this.permalinks ? this.getPageFromHash() : 1;
    this.currentOffset = (initialPage - 1) * this.limit + 1;

    // Show initial rows
    this.showRows(this.currentOffset, this.limit);
  },

  /**
   * Re-write location hash with current page and filters values
   */
  updateLocationHash: function()
  {
    var currentHash = window.location.hash.substring(1);
    var filterString = this.filter.serializeFilters();

    var shouldUpdate = this.lastoffset != 1 || !currentHash.blank() || !filterString.blank();

    if (shouldUpdate) {
      var tables = currentHash.split("|"), newHash = "";
      for (var i=0;i<tables.length;i++) {
        var params = tables[i].toQueryParams();
        if (params["t"] != this.domNodeName) {
          // Don't override other tables params
          newHash += (tables[i] + "|"); 
        }
      }
      newHash += "t=#{table}&p=#{page}".interpolate({
        "table" : this.domNodeName,
        "page" : ((this.lastoffset - 1) / this.limit) + 1
      });

      newHash += filterString;

      window.location.hash = "#" + newHash;
    }
  },

  /**
   * Returns the current page associated to this livetable.
   */
  getPageFromHash: function()
  {
    var hashString = window.location.hash.substring(1);
    if (!hashString.blank()) {
      var tables = hashString.split("|");
      for (var i=0;i<tables.length;i++) {
        var params = tables[i].toQueryParams();
        if (params["t"] == this.domNodeName && parseInt(params["p"])) {
          return parseInt(params["p"]);
        }
      }
    }
    return 1;
  },

  /**
   * Retrieve this table filters as filter key/filter value object from the window location hash.
   * Returns an empty object if no filter is defined in the hash for this table.
   */
  getFiltersFromHash: function()
  {
    var hashString = window.location.hash.substring(1);
    if (!hashString.blank()) {
      var tables = hashString.split("|");
      for (var i=0;i<tables.length;i++) {
        var params = tables[i].toQueryParams();
        if (params["t"] == this.domNodeName) { // that's our table !
          var parameterNames = Object.keys(params), result = new Object();
          for (var j=0;j<parameterNames.length;j++) {
            if (parameterNames[j] != "t" && parameterNames[j] != "p") { // ignore those reserved params
              result[parameterNames[j]] = params[parameterNames[j]];
            }
          }
          return result;
        }
      }
    }
    return new Object();
  },

  /**
    * Initializes an AJAX request for retrieving some table data. Uses two ranges, one defines the
    * range that must be retrieved from the server, and one defines the range that should be
    * displayed. Two ranges are needed as some of the displayed rows can already be available from
    * a previous request.
    * @param offset Starting request offset; the index of the first row that should be retrieved.
    * @param limit Maximum number of rows to retrieve.
    * @param doffset Starting display offset; the index of the first row that should be displayed.
    * @param dlimit Maximum number of rows to display.
    */
  getRows: function(reqOffset, reqLimit, displayOffset, displayLimit)
  {
    var url =  this.getUrl + '&offset='+reqOffset+'&limit='+reqLimit+'&reqNo='+ (++this.sendReqNo);

    if (this.filter) {
      this.filters = this.filter.serializeFilters();
      if (this.filters != undefined && this.filters != "") {
        url += this.filters;
      }
    }

    if (typeof this.tags != "undefined" && this.tags.length > 0) {
       this.tags.each(function(tag) {
          url += ("&tag=" + encodeURIComponent(tag.unescapeHTML()));
       });
    }

    url += this.getSortURLFragment();

    var self = this;

    this.loadingStatus.removeClassName("hidden");

    var ajx = new Ajax.Request(url,
    {
      method: 'get',
      onComplete: function( transport ) {
        self.loadingStatus.addClassName("hidden");
      },

      onSuccess: function( transport ) {
        var res = eval( '(' + transport.responseText + ')');

        if (res.reqNo < self.sendReqNo) {
          return;
        }

        self.recvReqNo = res.reqNo;
        self.loadingStatus.addClassName("hidden");

        if(self.tagCloud && res.matchingtags) {
           self.tagCloud.updateTagCloud(res.tags, res.matchingtags);
        }

        self.updateFetchedRows(res);
        self.displayRows(displayOffset, displayLimit);
      }
    });
  },

  /**
    * Add/refresh items to the cache of fetched data.
    * @param json Returned data from the server, as a parsed JSON object.
    */
  updateFetchedRows: function(json)
  {
    this.json = json;
    this.totalRows = json.totalrows;
    for (var i = json.offset; i < json.offset + json.returnedrows; ++i) {
      this.fetchedRows[i] = json.rows[i-json.offset];
    }
  },

  /**
    * Removes the displayed rows from the XHTML document.
    */
  clearDisplay: function()
  {
    var object = this.displayNode;
    while (object.hasChildNodes()) {
      object.removeChild(object.firstChild);
    }
  },

  /**
    * Displays already fetched rows. Calls {@link #handler} for creating the XHTML elements, and
    * inserts them in {@link domNode}.
    * @param offset Starting offset; the index of the first row that should be displayed.
    * @param limit Maximum number of rows to display.
    */
  displayRows: function(offset, limit)
  { 
    var f = offset + limit - 1;
    if (f > this.totalRows) f = this.totalRows;
    var off = (this.totalRows > 0) ? offset : 0;
    var msg = "<strong>" + off + "</strong> - <strong>" + f + "</strong> $msg.get('xe.pagination.results.of') <strong>" + this.totalRows + "</strong>";
    var msg = msg.toLowerCase();

    this.limitsDisplay.innerHTML = "$msg.get('xe.pagination.results') " + msg;
    this.clearDisplay();

    for (var i = off; i <= f; i++) {
      if (this.fetchedRows[i]) {
        var elem = this.handler(this.fetchedRows[i], i, this);      
        this.displayNode.appendChild(elem);
        document.fire("xwiki:livetable:newrow", {
          "row":elem,
          "table":this
        });
      }
    }
    if (this.paginator) this.paginator.refreshPagination();
  },

  /**
    * Fetch and display rows. This method checks the existing fetched data to determine which (if
    * any) rows should be fetched from the server, then forwards the call to {@link #displayRows}.
    * @param offset Starting offset; the index of the first row that should be displayed.
    * @param limit Maximum number of rows to display.
    */
  showRows: function(offset, limit)
  {
    this.lastoffset = offset;

    if (this.permalinks) {
      this.updateLocationHash();
    }

    // This is some debugging string.
    var buff  = 'request to display rows '+offset+' to '+(offset+limit)+' <br />\n';

    // If no rows fetched yet, get all we need
    if (this.totalRows == -1) {
      this.getRows(offset, limit, offset, limit);
      buff += 'table is empty so we get all rows';
      return buff;
    }

    // Make a range of required rows
    var min = -1;
    var max = -1;

    for (var i = offset; i < (offset + limit); ++i) {
      if (this.fetchedRows[i] == undefined) {
        if (min == -1) {
          min = i;
        }
        max = i;
      }
    }

    // If we don't need any new row
    if (min == -1) {
      buff += 'no need to get new rows <br />\n';
      this.displayRows(offset, limit);
    } else {
      // We need to get new rows
      buff += 'we need to get rows '+min+' to '+ (max+1) +' <br />\n';
      this.getRows(min, max - min + 1, offset, limit);
    }
 
    if(this.paginator) this.paginator.refreshPagination();

    return buff;
  },

  /**
    * Delete a row from the fetch cache, shifting the remaining rows accordingly.
    */
  deleteAndShiftRows: function(indx)
  {
    for(i in this.fetchedRows) {
      if(i >= indx)
      this.fetchedRows[i] = this.fetchedRows[''+(parseInt(i)+1)];
    }
  },

  /**
    * Debug method. Dumps the content of the fetch cache (row indexes only).
    */
  debugFetchedRows: function() {
    var buf = '';
    for (i in this.fetchedRows) {
      if (this.fetchedRows[i] != undefined) {
        buf += i+' ';
      }
    }
    return buf;
  },

  /**
    * Delete a row and redisplay the table.
    * @param indx The index of the row that must be deleted.
    */
  deleteRow: function(indx) {
    this.deleteAndShiftRows(indx);

    // Compute new refresh offset
    var newoffset = this.lastoffset;
    if(indx > this.totalRows - this.limit - 1) {
      newoffset -= 1;
    }
    if(newoffset <= 0) {
      newoffset = 1;
    }
    this.totalRows -= 1;
    if(this.totalRows < this.limit) {
      this.showRows(newoffset, this.totalRows);
    }
    else {
      this.showRows(newoffset, this.limit);
    }
                                     
    if (this.paginator) this.paginator.refreshPagination();
  },

  /**
   * Return the URL fragment with sort parameters depending on the state of the table.
   */
  getSortURLFragment:function() {
    var fragment = "&sort=";
    if (typeof $(this.domNodeName).down("th.selected a") != "undefined") {
       fragment += $(this.domNodeName).down("th.selected a").getAttribute('rel');
    }
    fragment += "&dir=";
    if (typeof $(this.domNodeName).down("th.selected") != "undefined") {
       fragment += ($(this.domNodeName).down("th.selected").hasClassName('desc') ? 'desc' : 'asc');
    }
    return fragment;
  },

  /**
   * Remove all the fetched data from the cache.
   */
  clearCache: function()
  {
    this.fetchedRows.clear();
    this.totalRows = -1;
  },

  /**
   * Iterate over the column headers that have the sortable class to observe sort changes when user clicks the column header.
   */
  observeSortableColumns: function(){
    var self = this;
    $(this.domNodeName).select('th.sortable').each(function(el) {
      if (el.hasClassName('selected')) {
         self.selectedColumn = el;
      }
      if(!el.hasClassName('desc') && !el.hasClassName('asc')) { // no order set in the HTML. Force desc
         el.addClassName('desc');
      }
      Event.observe(el, "click", function(event) {
         var elem = event.element();
         if (!elem.hasClassName('sortable')) {
            elem = elem.up('th.sortable');
         }
         if (elem == null) {
            // This should never happen in real life, but better safe than sorry...
            return;
         }
         if (elem.hasClassName("selected")) { // Sort column already selected. Change direction
            var direction = elem.hasClassName("asc") ? "asc" : "desc";
            var newDirection = direction == "asc" ? "desc" : "asc";
            elem.removeClassName(direction);
            elem.addClassName(newDirection);
         }
         else { // sort column was not selected, do not change direction, just column
            if (self.selectedColumn){
              self.selectedColumn.removeClassName("selected");
            }
            elem.addClassName("selected");
            self.selectedColumn = elem;
         }
         self.clearCache(); //reset
         self.showRows(1, self.limit);
      });
   });
  }
});

/**
 * Helper class to display pagination
 */
var LiveTablePagination = Class.create({
    initialize: function(table, domNodes, max)
    {
      this.table = table;
      var self = this;
      this.pagesNodes = [];
      domNodes.each(function(elem){
         self.pagesNodes.push(elem.down(".xwiki-livetable-pagination-content"));
      });
      this.max = max;
      $(this.table.domNodeName).select("span.prevPagination").invoke("observe", "click", this.gotoPrevPage.bind(this));
      $(this.table.domNodeName).select("span.nextPagination").invoke("observe", "click", this.gotoNextPage.bind(this));
    },
    refreshPagination: function()
    {
      var self = this;
      this.pagesNodes.each(function(elem){
         elem.innerHTML = "";
      });
      var pages = Math.ceil(this.table.totalRows / this.table.limit);
      var currentMax = (!this.max) ? pages : this.max;
      var currentPage = Math.floor( this.table.lastoffset / this.table.limit) + 1;
      var startPage = Math.floor(currentPage / currentMax) * currentMax - 1;
      // always display the first page
      if (startPage>1) {
         this.pagesNodes.each(function(elem){
             elem.insert(self.createPageLink(1, false));
         });
         if (startPage>2) {
            this.pagesNodes.invoke("insert", " ... ");
         }
      }
      // display pages 
      var i;
      for (i=(startPage<=0) ? 1 : startPage;i<=Math.min(startPage + currentMax + 1, pages);i++) {
         var selected = (currentPage == i) ? true : false
         this.pagesNodes.each(function(elem){
             elem.insert(self.createPageLink(i, selected));
         });
         this.pagesNodes.invoke("insert", " ");
      }
      // alwyas display the last page.
      if (i<pages) {
        if (i+1 < pages) {
          this.pagesNodes.invoke("insert", " ... ");
        }
        //this.pagesNodes.invoke("insert", pageSpan.clone());
        this.pagesNodes.each(function(elem){
             elem.insert(self.createPageLink(pages, false));
        });
      }
    },
    createPageLink:function(page, selected) {
        var pageSpan = new Element("span", {'class':'pagenumber'}).update(page);
        if (selected) {
           pageSpan.addClassName("selected");
        }
        var self = this;
        pageSpan.observe("click", function(ev){
            self.gotoPage(ev.element().innerHTML);
        });
        return pageSpan;
    },
    gotoPage: function(page)
    {
      this.table.showRows(((parseInt(page) - 1 )* this.table.limit) + 1, this.table.limit);
    },
    gotoPrevPage: function() {
      var currentPage = Math.floor( this.table.lastoffset / this.table.limit) + 1;
      var prevPage = currentPage - 1;
      if (prevPage > 0) {
        this.table.showRows(((parseInt(prevPage) - 1) * this.table.limit) + 1, this.table.limit);
      }
    },
    gotoNextPage: function() {
      var currentPage = Math.floor( this.table.lastoffset / this.table.limit) + 1;
      var pages = Math.ceil(this.table.totalRows / this.table.limit);
      var nextPage = currentPage + 1;
      if (nextPage <= pages) {
        this.table.showRows(((parseInt(nextPage) - 1) * this.table.limit) + 1, this.table.limit);
      }
    }
});


/*
 * The class that deals with the filtering in a table
 */
var LiveTableFilter = Class.create({
  initialize: function(table, filterNode, filters)
  {
    this.table = table;
    this.filterNode = $(filterNode);
    this.filters = new Object();

    this.filters = filters;
    this.initializeFilters();

    this.attachEventHandlers();
  },

  makeRefreshHandler: function(self)
  {
    return function() {
      self.refreshContent();
    }
  },

  /**
   * Initialize DOM values of the filters elements based on the passed map of name/value.
   * TODO: rewrite this method the other way around (iterate on the map, not on the filters).
   */
  initializeFilters: function()
  {
    var inputs = this.filterNode.select("input");
    for(var i=0;i<inputs.length;i++) {
       var key=inputs[i].name;
       if(inputs[i].type=="radio" || inputs[i].type=="checkbox") {
          if(this.filters[key] && this.filters[key] == inputs[i].value.strip())
            inputs[i].checked = true;
          else
            inputs[i].checked = false;
       } else {
          if(this.filters[key])
            inputs[i].value = this.filters[key];
       }
     }

     var selects = this.filterNode.select("select");
     for(var i=0;i<selects.length;i++) {
        for (var j=0;j<selects[i].options.length;j++) {
        if (this.filters[selects[i].name] && selects[i].options[j].value == this.filters[selects[i].name]) {
           selects[i].options[j].selected = true;
        } else {
           selects[i].options[j].selected = false;
        } 
      }
     }
  },

  serializeFilters: function()
  {
    // It's a shame we can't use prototype Form methods on non-form elements.
    // In the future, we need to have the livetable filters in a real form (for a degraded version w/o js)
    // Then we can write :
    // return Form.serializeElements(Form.getElements(this.domNodeName);
    var result = "";
    var filters = this.filterNode.select("input", "select");
    for (var i=0;i<filters.length;i++) {
      if (!filters[i].value.blank()) {
	    if ((filters[i].type != "radio" && filters[i].type != "checkbox") || filters[i].checked) {
          result += ("&" + filters[i].name + "=" + encodeURIComponent(filters[i].value));
        }
      }
    }
    return result;
  },


  attachEventHandlers: function()
  {
    var inputs = this.filterNode.getElementsByTagName('input');
    var selects = this.filterNode.getElementsByTagName('select');

    for(var i = 0; i < inputs.length; i++) {
      if (inputs[i].type == "text") {
        Event.observe(inputs[i], 'keyup', this.makeRefreshHandler(this));
      } else {
        //IE is buggy on "change" events for checkboxes and radios
        Event.observe(inputs[i], 'click', this.makeRefreshHandler(this));
      }
    }

    for(var i = 0; i < selects.length; i++) {
      Event.observe(selects[i], 'change', this.makeRefreshHandler(this));
    }
  },

  /**
    * Refresh the table when the filters have changed.
    */
  refreshContent : function()
  {
    var newFilters = this.serializeFilters();
    if (newFilters == this.table.filters) {
      return;
    }
    this.table.totalRows = -1;
    this.table.fetchedRows = new Array();
    this.table.filters = newFilters;
    this.table.showRows(1, this.table.limit);
  // 0 was 1
  }
});

/**
 * Helper class to filter on tags/display tags matching filters.
 */
var LiveTableTagCloud = Class.create({
   /**
    * Constructor.
    */
   initialize: function(table, domNodeName, tags) {
      this.table = table;
      this.domNode = $(domNodeName);
      this.cloudFilter = false;
      if (typeof tags == "array") {
         this.tags = tags;
         if (tags.length > 0) {
           this.updateTagCloud(tags);
         }
      }
   },

   /**
    * Tags cardinality map. Empty at first.
    */
   tags: [],

   /**
    * Tags matching the current filters
    */
   matchingTags: [],

   /**
    * Tags selected as filters
    */
   selectedTags: {},
   
   /**
    * Default popularity levels. Used as CSS class on the tag list items.
    */
   popularityLevels: ["notPopular", "notVeryPopular", "somewhatPopular", "popular", "veryPopular", "ultraPopular"],

   /**
    * Update the tag cloud with new tags values.
    * This is the only hook the table will call us from.
    */
   updateTagCloud: function(tags, matchingTags) {
      if (!this.hasTags && tags.length > 0) {
        this.tags = tags;    
        this.map = this.buildPopularityMap(this.tags);
        this.hasTags = true;
        this.domNode.removeClassName("hidden");
      }
      this.matchingTags = matchingTags;       
      this.displayTagCloud();
   },

   displayTagCloud: function(){
      this.domNode.down('.xwiki-livetable-tagcloud').innerHTML = "";
      var cloud = new Element("ol", {'class':'tagCloud'});
      var liClass;
      for (var i=0;i<this.tags.length;i++) {
         liClass = "";
         var levels = this.map.keys().reverse();
         for (var j=0;j<levels.length;j++) {
            if (this.tags[i].count >= levels[j] || (j == (levels.length - 1))) {
               liClass = this.map.get(levels[j]);
               break;
            }
         }
         var tagLabel = this.tags[i].tag;
         var tagSpan = new Element("span").update(tagLabel.escapeHTML());
         var tag = new Element("li", {'class':liClass}).update(tagSpan);
         if (typeof this.matchingTags[tagLabel] != "undefined") {
            tag.addClassName("selectable");
            Event.observe(tagSpan, "click", function(event) {
                var tag = event.element().up("li").down("span").innerHTML.unescapeHTML();
                event.element().up("li").toggleClassName("selected");
                if (event.element().up("li").hasClassName("selected")) {
                  self.selectedTags[tag] = {};
                }
                else {
                  delete self.selectedTags[tag];
                }
                self.table.tags = self.getSelectedTags();
                self.table.totalRows = -1;
                self.table.fetchedRows = new Array();
                self.table.showRows(1, self.table.limit);
            });
         }
         if (this.selectedTags[tagLabel] != undefined) {
            tag.addClassName("selected");
         }
         var self = this;
         tag.appendChild(document.createTextNode(" "));
         cloud.appendChild(tag);
      }
      this.domNode.down('.xwiki-livetable-tagcloud').appendChild(cloud);
   },

   getSelectedTags: function() {
      var result = new Array();
      this.domNode.select("li.selected").each(function(tag) {
         result.push(tag.down("span").innerHTML);
      });
      return result;
   },

   /**
    * Transform the cardinality map of tags in a map of CSS classes.
    * Ported to JS from XWiki.TagCloud for consistency.
    */
   buildPopularityMap:function(tags){
      var totalCount = 0;
      var minCount = 0;
      var maxCount = -1;
      tags.each(function(tag){
          totalCount += tag.count;
          if(tag.count < minCount || minCount === 0) {
              minCount = tag.count;
          }
          if(tag.count > maxCount || maxCount === -1) {
              maxCount = tag.count;
          }
      });
      var countAverage = totalCount / tags.length;
      var levelsHalf = this.popularityLevels.length / 2;
      var firstHalfCountDelta = countAverage - minCount;
      var secondHalfCountDelta = maxCount - countAverage;

      var firstHalfIntervalSize = firstHalfCountDelta / levelsHalf;
      var secondHalfIntervalSize = secondHalfCountDelta / levelsHalf;
      var previousPopularityMax = minCount;
      var intervalSize = firstHalfIntervalSize;
      var halfPassed = false;
      var count = 0;
      var currentPopularityMax;

      var popularityMap = new Hash();

      this.popularityLevels.each(function(level){
         count++;
         if(count > levelsHalf && !halfPassed) {
              intervalSize = secondHalfIntervalSize;
              halfPassed = true;
         }
         currentPopularityMax = previousPopularityMax + intervalSize;
         popularityMap.set(currentPopularityMax, level);
         previousPopularityMax = currentPopularityMax;
      });

      return popularityMap;
   }
});


/**
 * The Ugly: Fix IE6
 * Add specific classes when mouse is over table rows, since it cannot be handled in CSS.
 */
if(browser.isIE6x) {
  // get notified of all new rows created by live tables.
  document.observe("xwiki:livetable:newrow", function(ev) {
    // Add events listeners to mouse over/out on the <tr>
    Event.observe(ev.memo.row, "mouseover", function(event){
      event.element().up("tr").addClassName("rowHover");
    });
    Event.observe(ev.memo.row, "mouseout", function(event){
      event.element().up("tr").removeClassName("rowHover");
     });
  });
}

})();

