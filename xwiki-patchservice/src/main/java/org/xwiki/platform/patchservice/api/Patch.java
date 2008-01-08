package org.xwiki.platform.patchservice.api;

import java.util.List;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * <p>
 * A <tt>Patch</tt> reflects the changes between two consecutive versions of a document. A patch
 * consists of several {@link Operation operations} that change parts of a document, and carries the
 * identity of the host where it was originally created.
 * </p>
 * <p>
 * A host can generate patches as new document versions are created by the users, which can then be
 * transmitted to other nodes hosting the same wiki and applied to ensure a consistency among all
 * the nodes hosting a wiki. If a patch cannot be applied on a host, then an exception is thrown.
 * </p>
 * <p>
 * It is not the patch's role to assure consistency among hosts, they just reflect changes on one
 * node.
 * </p>
 * 
 * @see RWPatch
 * @version $Id: $
 * @since XWikiPlatform 1.3
 */
public interface Patch extends XmlSerializable
{
    /**
     * Returns the version of the patch model used in this patch. This allows the patch model do
     * evolve, while allowing older patches to still be recognized and used in a newer platform.
     * 
     * @return The patch model specification used by this patch.
     */
    String getSpecVersion();

    /**
     * Returns a {@link PatchId patch identifier}.
     * 
     * @return The Identifier of this patch.
     * @see PatchId
     */
    PatchId getId();

    /**
     * Get a description for this patch. This can be the edit comment, for example.
     * 
     * @return The patch description.
     */
    String getDescription();

    /**
     * Return a {@link Originator patch originator} object, identifying the person/place where the
     * changes were performed.
     * 
     * @return The patch originator.
     */
    Originator getOriginator();

    /**
     * Get the list of operations in this patch.
     * 
     * @return The list of patch operations.
     */
    List getOperations();

    /**
     * Apply this patch on a document. The object will be changed by the patch in a non-transaction
     * manner, meaning that if an exception occurs in an operation, the operations already applied
     * are not rolled-back.
     * 
     * @param doc The document being patched.
     * @param context The XWiki context, needed for some document operations.
     * @throws XWikiException If the patch cannot be applied on the document.
     */
    void apply(XWikiDocument doc, XWikiContext context) throws XWikiException;
}
