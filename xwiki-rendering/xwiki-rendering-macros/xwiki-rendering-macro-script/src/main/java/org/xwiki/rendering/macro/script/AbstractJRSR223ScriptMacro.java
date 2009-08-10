/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.macro.script;

import java.io.StringWriter;
import java.util.List;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.ContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.script.ScriptContextManager;

/**
 * Base Class for script evaluation macros based on JSR223.
 * 
 * @param <P> the type of macro parameters bean.
 * @version $Id$
 * @since 1.7M3
 */
public abstract class AbstractJRSR223ScriptMacro<P extends JSR223ScriptMacroParameters> extends AbstractScriptMacro<P>
{
    /**
     * Used to get the current script context to give to script engine evaluation method.
     */
    @Requirement
    private ScriptContextManager scriptContextManager;

    /**
     * Used to find if the current document's author has programming rights.
     */
    @Requirement
    private DocumentAccessBridge documentAccessBridge;

    /**
     * @param macroName the name of the macro (eg "groovy")
     */
    public AbstractJRSR223ScriptMacro(String macroName)
    {
        super(macroName, null, JSR223ScriptMacroParameters.class);
    }

    /**
     * @param macroName the name of the macro (eg "groovy")
     * @param macroDescription the text description of the macro.
     */
    public AbstractJRSR223ScriptMacro(String macroName, String macroDescription)
    {
        super(macroName, macroDescription, JSR223ScriptMacroParameters.class);
    }

    /**
     * @param macroName the name of the macro (eg "groovy")
     * @param macroDescription the text description of the macro.
     * @param contentDescriptor the description of the macro content.
     */
    public AbstractJRSR223ScriptMacro(String macroName, String macroDescription, ContentDescriptor contentDescriptor)
    {
        super(macroName, macroDescription, contentDescriptor, JSR223ScriptMacroParameters.class);
    }

    /**
     * @param macroName the name of the macro (eg "groovy")
     * @param macroDescription the text description of the macro.
     * @param parametersBeanClass class of the parameters bean for this macro.
     */
    public AbstractJRSR223ScriptMacro(String macroName, String macroDescription,
        Class< ? extends JSR223ScriptMacroParameters> parametersBeanClass)
    {
        super(macroName, macroDescription, parametersBeanClass);
    }

    /**
     * @param macroName the name of the macro (eg "groovy")
     * @param macroDescription the text description of the macro.
     * @param contentDescriptor the description of the macro content.
     * @param parametersBeanClass class of the parameters bean for this macro.
     */
    public AbstractJRSR223ScriptMacro(String macroName, String macroDescription, ContentDescriptor contentDescriptor,
        Class< ? extends JSR223ScriptMacroParameters> parametersBeanClass)
    {
        super(macroName, macroDescription, contentDescriptor, parametersBeanClass);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.macro.Macro#supportsInlineMode()
     */
    public boolean supportsInlineMode()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.macro.script.AbstractScriptMacro#execute(java.lang.Object, java.lang.String,
     *      org.xwiki.rendering.transformation.MacroTransformationContext)
     */
    @Override
    public List<Block> execute(P parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        if (!canExecuteScript()) {
            throw new MacroExecutionException("You don't have the right to execute this script");
        }

        return super.execute(parameters, content, context);
    }

    /**
     * Method to overwrite to indicate the script engine name.
     * 
     * @param parameters the macro parameters.
     * @param context the context of the macro transformation.
     * @return the name of the script engine to use.
     */
    protected String getScriptEngineName(P parameters, MacroTransformationContext context)
    {
        return context.getCurrentMacroBlock().getId().toLowerCase();
    }

    /**
     * Get the current ScriptContext and refresh it.
     * 
     * @return the script context.
     */
    protected ScriptContext getScriptContext()
    {
        return this.scriptContextManager.getScriptContext();
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractScriptMacro#evaluate(ScriptMacroParameters, String, MacroTransformationContext) 
     */
    @Override
    protected String evaluate(P parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        if (StringUtils.isEmpty(content)) {
            return "";
        }

        String engineName = getScriptEngineName(parameters, context);

        String scriptResult;
        if (engineName != null) {
            // 2) execute script
            try {
                ScriptEngineManager sem = new ScriptEngineManager();
                ScriptEngine engine = sem.getEngineByName(engineName);
                if (engine != null) {
                    ScriptContext scriptContext = getScriptContext();

                    StringWriter stringWriter = new StringWriter();

                    // set writer in script context
                    scriptContext.setWriter(stringWriter);

                    eval(content, engine, scriptContext);

                    // remove writer script from context
                    scriptContext.setWriter(null);

                    scriptResult = stringWriter.toString();
                } else {
                    throw new MacroExecutionException("Can't find script engine with name [" + engineName + "]");
                }
            } catch (ScriptException e) {
                throw new MacroExecutionException("Failed to evaluate Script Macro for content [" + content + "]", e);
            }
        } else {
            // If no language identifier is provided, don't evaluate content
            scriptResult = content;
        }

        return scriptResult;
    }

    /**
     * Execute the script.
     * 
     * @param content the script to be executed by the script engine
     * @param engine the script engine
     * @param scriptContext the script context
     * @return The value returned from the execution of the script.
     * @throws ScriptException if an error occurrs in script. ScriptEngines should create and throw
     *             <code>ScriptException</code> wrappers for checked Exceptions thrown by underlying scripting
     *             implementations.
     */
    protected Object eval(String content, ScriptEngine engine, ScriptContext scriptContext) throws ScriptException
    {
        return engine.eval(content, scriptContext);
    }

    /**
     * Indicate if the script is executable in the current context.
     * <p>
     * For example with not protected script engine, we are testing if the current dcument's author has "programming"
     * right.
     * 
     * @return true if the script can be evaluated, false otherwise.
     */
    protected boolean canExecuteScript()
    {
        return this.documentAccessBridge.hasProgrammingRights();
    }

    // /////////////////////////////////////////////////////////////////////
    // Compiled scripts management

    /**
     * Return a compiled version of the provided script.
     * 
     * @param content the script to compile.
     * @param engine the script engine.
     * @return the compiled version of the script.
     * @throws ScriptException failed to compile the script.
     */
    protected CompiledScript getCompiledScript(String content, Compilable engine) throws ScriptException
    {
        // TODO: add caching

        return engine.compile(content);
    }
}
