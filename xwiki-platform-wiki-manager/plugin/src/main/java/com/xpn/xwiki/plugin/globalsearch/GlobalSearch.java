package com.xpn.xwiki.plugin.globalsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.applicationmanager.core.plugin.XWikiPluginMessageTool;
import com.xpn.xwiki.plugin.globalsearch.tools.GlobalSearchQuery;
import com.xpn.xwiki.plugin.globalsearch.tools.GlobalSearchResult;

/**
 * Tool to be able to make and merge multi wikis search queries.
 * 
 * @version $Id: $
 */
final class GlobalSearch
{
    /**
     * The logging tool.
     */
    protected static final Log LOG = LogFactory.getLog(GlobalSearch.class);

    /**
     * Hql "select" keyword.
     */
    private static final String SELECT_KEYWORD = "select";

    /**
     * Hql "select distinct" keyword.
     */
    private static final String SELECT_DISTINCT_KEYWORD = "select distinct";

    /**
     * Hql "from" keyword.
     */
    private static final String FROM_KEYWORD = "from";

    /**
     * Hql "where" keyword.
     */
    private static final String WHERE_KEYWORD = "where";

    /**
     * Hql "order by" keyword.
     */
    private static final String ORDER_KEYWORD = "order by";

    /**
     * Hql "order by" descendant keyword.
     */
    private static final String ORDER_DESC = "desc";

    /**
     * Name of the field containing document space in the HQL query.
     */
    private static final String HQL_DOC_SPACE = "doc.web";

    /**
     * Name of the field containing document name in the HQL query.
     */
    private static final String HQL_DOC_NAME = "doc.name";

    /**
     * The searchDocument and searchDocumentsNames initial select query part.
     */
    private static final String SEARCHDOC_INITIAL_SELECT = "select distinct doc.web, doc.name";

    /**
     * The searchDocument and searchDocumentsNames initial select query part when distinct documents
     * by language.
     */
    private static final String SEARCHDOC_INITIAL_SELECT_LANG =
        "select distinct doc.web, doc.name, doc.language";

    /**
     * The searchDocument and searchDocumentsNames initial from query part.
     */
    private static final String SEARCHDOC_INITIAL_FROM = " from XWikiDocument as doc";

    /**
     * Comma.
     */
    private static final String FIELD_SEPARATOR = ",";

    // ////////////////////////////////////////////////////////////////////////////

    /**
     * The plugin internationalization service.
     */
    private XWikiPluginMessageTool messageTool;

    /**
     * Hidden constructor of GlobalSearch only access via getInstance().
     * 
     * @param messageTool the plugin internationalization service.
     */
    GlobalSearch(XWikiPluginMessageTool messageTool)
    {
        this.messageTool = messageTool;
    }

    // ////////////////////////////////////////////////////////////////////////////

    /**
     * @param context the XWiki context.
     * @return the names of all virtual wikis.
     * @throws XWikiException error when getting the list of virtual wikis.
     */
    private Collection getAllWikiNameList(XWikiContext context) throws XWikiException
    {
        Collection wikiNames = context.getWiki().getVirtualWikisDatabaseNames(context);

        if (!wikiNames.contains(context.getMainXWiki())) {
            wikiNames.add(context.getMainXWiki());
        }

        return wikiNames;
    }

    /**
     * Execute query in all provided wikis and return list containing all results. Compared to XWiki
     * Platform search, searchDocuments and searchDocumentsName it's potentially "time-consuming"
     * since it issues one request per provided wiki.
     * 
     * @param query the query parameters.
     * @param context the XWiki context.
     * @return the search result as list of {@link GlobalSearchResult} containing all selected
     *         fields values.
     * @throws XWikiException error when executing query.
     */
    public Collection search(GlobalSearchQuery query, XWikiContext context) throws XWikiException
    {
        List resultList = Collections.EMPTY_LIST;

        List selectColumns = parseSelectColumns(query.getHql());
        List orderColumns = parseOrderColumns(query.getHql());

        Collection wikiNameList;
        if (context.getWiki().isVirtual()) {
            wikiNameList = query.getWikiNameList();
            if (wikiNameList.isEmpty()) {
                wikiNameList = getAllWikiNameList(context);
            }
        } else {
            wikiNameList = new ArrayList(1);
            wikiNameList.add(context.getMainXWiki());
        }

        int max =
            query.getMax() > 0 ? query.getMax() + (query.getStart() > 0 ? query.getStart() : 0)
                : 0;

        String database = context.getDatabase();
        try {
            resultList = new LinkedList();

            for (Iterator it = wikiNameList.iterator(); it.hasNext();) {
                String wikiName = (String) it.next();

                context.setDatabase(wikiName);

                List resultsTmp =
                    context.getWiki().getStore().search(query.getHql(), max, 0,
                        query.getParameterList(), context);

                insertResults(wikiName, resultList, resultsTmp, query, selectColumns,
                    orderColumns, context);
            }
        } finally {
            context.setDatabase(database);
        }

        if (resultList.size() > max || query.getStart() > 0) {
            resultList =
                resultList.subList(query.getStart() > 0 ? query.getStart() : 0,
                    resultList.size() > max ? max : resultList.size());
        }

        return resultList;
    }

    /**
     * Insert a list of result in the sorted main list.
     * 
     * @param wikiName the name of the wiki from where the list <code>list</code> come.
     * @param sortedList the sorted main list.
     * @param list the list to insert.
     * @param query the query parameters.
     * @param selectColumns the names of selected fields.
     * @param orderColumns the fields to order.
     * @param context the XWiki context.
     */
    private void insertResults(String wikiName, List sortedList, Collection list,
        GlobalSearchQuery query, List selectColumns, List orderColumns, XWikiContext context)
    {
        boolean sort = !sortedList.isEmpty();

        for (Iterator it = list.iterator(); it.hasNext();) {
            Object[] objects = (Object[]) it.next();

            GlobalSearchResult result = new GlobalSearchResult(wikiName, selectColumns, objects);

            if (sort) {
                insertResult(sortedList, result, query, selectColumns, orderColumns, context);
            } else {
                sortedList.add(result);
            }
        }
    }

    /**
     * Insert a result of result in the sorted main list.
     * 
     * @param sortedList the sorted main list.
     * @param result the fields values to insert.
     * @param query the query parameters.
     * @param selectColumns the names of selected fields.
     * @param orderColumns the fields to order.
     * @param context the XWiki context.
     */
    private void insertResult(List sortedList, GlobalSearchResult result,
        GlobalSearchQuery query, List selectColumns, List orderColumns, XWikiContext context)
    {
        int max =
            query.getMax() > 0 ? query.getMax() + (query.getStart() > 0 ? query.getStart() : 0)
                : -1;

        int index = 0;
        for (Iterator itSorted = sortedList.iterator(); itSorted.hasNext()
            && (max <= 0 || index < max); ++index) {
            GlobalSearchResult sortedResult = (GlobalSearchResult) itSorted.next();

            if (compare(sortedResult, result, orderColumns) > 0) {
                break;
            }
        }

        if (max <= 0 || index < max) {
            sortedList.add(index, result);
        }
    }

    /**
     * Compare two results depends on list of order fields.
     * 
     * @param result1 the first result to compare.
     * @param result2 the second result to compare.
     * @param orderColumns the list of order fields.
     * @return a negative integer, zero, or a positive integer as <code>map1</code> is less than,
     *         equal to, or greater than <code>map2</code>.
     */
    private int compare(GlobalSearchResult result1, GlobalSearchResult result2, List orderColumns)
    {
        for (Iterator it = orderColumns.iterator(); it.hasNext();) {
            int result = compare(result1, result2, (Object[]) it.next());

            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    /**
     * Compare two results depends on order fields.
     * 
     * @param result1 the first result to compare.
     * @param result2 the second result to compare.
     * @param orderField the order fields.
     * @return a negative integer, zero, or a positive integer as <code>map1</code> is less than,
     *         equal to, or greater than <code>map2</code>.
     */
    private int compare(GlobalSearchResult result1, GlobalSearchResult result2,
        Object[] orderField)
    {
        int result = 0;

        String fieldName = (String) orderField[0];
        boolean fieldAsc = ((Boolean) orderField[1]).booleanValue();

        Object value1 = result1.get(fieldName);
        Object value2 = result2.get(fieldName);

        if (value1 instanceof String) {
            result = ((String) value1).compareToIgnoreCase((String) value2);
        } else if (value1 instanceof Comparable) {
            result = ((Comparable) value1).compareTo(value2);
        }

        return fieldAsc ? result : -result;
    }

    /**
     * Extract names of selected fields from hql query.
     * 
     * @param hql the hql query. The hql has some constraints:
     *            <ul>
     *            <li>"*" is not supported in SELECT clause.</li>
     *            <li>All ORDER BY fields has to be listed in SELECT clause.</li>
     *            </ul>
     * @return the names of selected fields from hql query.
     */
    private List parseSelectColumns(String hql)
    {
        List columnList = new ArrayList();

        int selectEnd = 0;
        int selectIndex = hql.toLowerCase().indexOf(SELECT_DISTINCT_KEYWORD);
        if (selectIndex < 0) {
            selectIndex = hql.toLowerCase().indexOf(SELECT_KEYWORD);

            if (selectIndex < 0) {
                selectIndex = 0;
            } else {
                selectEnd = SELECT_KEYWORD.length();
            }
        } else {
            selectEnd = SELECT_DISTINCT_KEYWORD.length();
        }

        int fromIndex = hql.toLowerCase().indexOf(FROM_KEYWORD);

        if (fromIndex >= 0) {
            String selectContent = hql.substring(selectIndex + selectEnd + 1, fromIndex);
            String[] columnsTable = selectContent.split(FIELD_SEPARATOR);
            for (int i = 0; i < columnsTable.length; ++i) {
                String[] column = columnsTable[i].trim().split("\\s");
                String columnName = column[0];
                columnList.add(columnName);
            }
        }

        return columnList;
    }

    /**
     * Extract names of "order by" fields from hql query.
     * 
     * @param hql the hql query.
     * @return the names of "order by" fields from hql query.
     */
    private List parseOrderColumns(String hql)
    {
        List columnList = new ArrayList();

        int orderIndex = hql.toLowerCase().lastIndexOf(ORDER_KEYWORD);

        if (orderIndex >= 0) {
            String orderContent = hql.substring(orderIndex + ORDER_KEYWORD.length() + 1);
            String[] columnsTable = orderContent.split(FIELD_SEPARATOR);
            for (int i = 0; i < columnsTable.length; ++i) {
                String orderField = columnsTable[i];
                Object[] orderFieldTable = orderContent.split("\\s+");

                orderField = (String) orderFieldTable[0];

                Boolean asc = Boolean.TRUE;
                if (orderFieldTable.length > 1
                    && ((String) orderFieldTable[1]).trim().toLowerCase().equals(ORDER_DESC)) {
                    asc = Boolean.FALSE;
                }

                columnList.add(new Object[] {orderField.trim(), asc});
            }
        }

        return columnList;
    }

    /**
     * @param queryPrefix the start of the SQL query (for example "select distinct doc.web,
     *            doc.name")
     * @param whereSQL the where clause to append
     * @return the full formed SQL query, to which the order by columns have been added as returned
     *         columns (this is required for example for HSQLDB).
     */
    protected String createSearchDocumentsHQLQuery(String queryPrefix, String whereSQL)
    {
        StringBuffer hql = new StringBuffer(queryPrefix);

        String normalizedWhereSQL;
        if (whereSQL == null) {
            normalizedWhereSQL = "";
        } else {
            normalizedWhereSQL = whereSQL.trim();
        }

        Collection orderColumns = parseOrderColumns(normalizedWhereSQL);

        for (Iterator it = orderColumns.iterator(); it.hasNext();) {
            Object[] orderField = (Object[]) it.next();
            if (!orderField.equals(HQL_DOC_SPACE) && !orderField.equals(HQL_DOC_NAME)) {
                hql.append(FIELD_SEPARATOR);
                hql.append(orderField[0]);
            }
        }

        hql.append(SEARCHDOC_INITIAL_FROM);

        if (normalizedWhereSQL.length() != 0) {
            if ((!normalizedWhereSQL.startsWith(WHERE_KEYWORD))
                && (!normalizedWhereSQL.startsWith(FIELD_SEPARATOR))) {
                hql.append(" ");
                hql.append(WHERE_KEYWORD);
                hql.append(" ");
            } else {
                hql.append(" ");
            }
            hql.append(normalizedWhereSQL);
        }

        return hql.toString();
    }

    /**
     * Search wiki pages in all provided wikis and return list containing found
     * {@link XWikiDocument}. Compared to XWiki Platform search, searchDocuments and
     * searchDocumentsName it's potentially "time-consuming" since it issues one request per
     * provided wiki.
     * 
     * @param query the query parameters.
     * @param distinctbylanguage when a document has multiple version for each language it is
     *            returned as one document a language.
     * @param customMapping inject custom mapping in session.
     * @param checkRight if true check for each found document if context's user has "view" rights
     *            for it.
     * @param context the XWiki context.
     * @return the found {@link XWikiDocument}.
     * @throws XWikiException error when searching for documents.
     */
    private Collection searchDocumentsNamesInfos(GlobalSearchQuery query,
        boolean distinctbylanguage, boolean customMapping, boolean checkRight,
        XWikiContext context) throws XWikiException
    {
        if (!query.getHql().toLowerCase().startsWith(SELECT_KEYWORD)) {
            String select =
                distinctbylanguage ? SEARCHDOC_INITIAL_SELECT_LANG : SEARCHDOC_INITIAL_SELECT;

            query.setHql(createSearchDocumentsHQLQuery(select, query.getHql()));
        }

        return search(query, context);
    }

    /**
     * Search wiki pages in all provided wikis and return list containing found
     * {@link XWikiDocument}. Compared to XWiki Platform search, searchDocuments and
     * searchDocumentsName it's potentially "time-consuming" since it issues one request per
     * provided wiki.
     * 
     * @param query the query parameters.
     * @param distinctbylanguage when a document has multiple version for each language it is
     *            returned as one document a language.
     * @param customMapping inject custom mapping in session.
     * @param checkRight if true check for each found document if context's user has "view" rights
     *            for it.
     * @param context the XWiki context.
     * @return the found {@link XWikiDocument}.
     * @throws XWikiException error when searching for documents.
     */
    public Collection searchDocuments(GlobalSearchQuery query, boolean distinctbylanguage,
        boolean customMapping, boolean checkRight, XWikiContext context) throws XWikiException
    {
        Collection results =
            searchDocumentsNamesInfos(query, distinctbylanguage, customMapping, checkRight,
                context);

        List documents = new ArrayList(results.size());

        String database = context.getDatabase();
        try {
            for (Iterator it = results.iterator(); it.hasNext();) {
                GlobalSearchResult result = (GlobalSearchResult) it.next();

                XWikiDocument doc = new XWikiDocument();
                doc.setSpace((String) result.get(HQL_DOC_SPACE));
                doc.setName((String) result.get(HQL_DOC_NAME));

                context.setDatabase(result.getWikiName());

                doc = context.getWiki().getStore().loadXWikiDoc(doc, context);

                if (checkRight) {
                    if (!context.getWiki().getRightService().checkAccess("view", doc, context)) {
                        continue;
                    }
                }

                documents.add(doc);
            }
        } finally {
            context.setDatabase(database);
        }

        return documents;
    }

    /**
     * Search wiki pages in all provided wikis and return list containing found
     * {@link XWikiDocument}. Compared to XWiki Platform search, searchDocuments and
     * searchDocumentsName it's potentially "time-consuming" since it issues one request per
     * provided wiki.
     * 
     * @param query the query parameters.
     * @param distinctbylanguage when a document has multiple version for each language it is
     *            returned as one document a language.
     * @param customMapping inject custom mapping in session.
     * @param checkRight if true check for each found document if context's user has "view" rights
     *            for it.
     * @param context the XWiki context.
     * @return the found {@link XWikiDocument}.
     * @throws XWikiException error when searching for documents.
     */
    public Collection searchDocumentsNames(GlobalSearchQuery query, boolean distinctbylanguage,
        boolean customMapping, boolean checkRight, XWikiContext context) throws XWikiException
    {
        Collection results =
            searchDocumentsNamesInfos(query, distinctbylanguage, customMapping, checkRight,
                context);

        List documentsNames = new ArrayList(results.size());

        for (Iterator it = results.iterator(); it.hasNext();) {
            GlobalSearchResult result = (GlobalSearchResult) it.next();

            documentsNames.add(result.getWikiName() + ":" + result.get(HQL_DOC_SPACE) + "."
                + result.get(HQL_DOC_NAME));
        }

        return documentsNames;
    }
}
