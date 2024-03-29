// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.javacc.java;

import org.javacc.java.JavaFiles.JavaResourceTemplateLocations;
import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParserConstants;
import org.javacc.parser.LexGen;
import org.javacc.parser.MetaParseException;
import org.javacc.parser.Options;
import org.javacc.parser.RStringLiteral;
import org.javacc.parser.RegExprSpec;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.Token;
import org.javacc.parser.TokenProduction;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the Constants file.
 */
public class OtherFilesGen extends JavaCCGlobals implements JavaCCParserConstants {

  private static final String CONSTANTS_FILENAME_SUFFIX = "Constants.java";

  static public void start(boolean isJavaModern) throws MetaParseException {
    
  JavaResourceTemplateLocations templateLoc = isJavaModern ? JavaFiles.RESOURCES_JAVA_MODERN : JavaFiles.RESOURCES_JAVA_CLASSIC;
    
    Token t = null;

    if (JavaCCErrors.get_error_count() != 0) throw new MetaParseException();

    // Added this if condition -- 2012/10/17 -- cba
  if ( Options.isGenerateBoilerplateCode()) {
      if (isJavaModern) {
        JavaFiles.gen_JavaModernFiles();
      }
      
      JavaFiles.gen_TokenMgrError(templateLoc);
      JavaFiles.gen_ParseException(templateLoc);
      JavaFiles.gen_Token(templateLoc);
  }


    if (Options.getUserTokenManager()) {
      // CBA -- I think that Token managers are unique so will always be generated
      JavaFiles.gen_TokenManager(templateLoc);
    } else if (Options.getUserCharStream()) {
      // Added this if condition -- 2012/10/17 -- cba
      if (Options.isGenerateBoilerplateCode()) {
        JavaFiles.gen_CharStream(templateLoc);
      }
    } else {
         // Added this if condition -- 2012/10/17 -- cba

      if (Options.isGenerateBoilerplateCode()) {
        if (Options.getJavaUnicodeEscape()) {
          JavaFiles.gen_JavaCharStream(templateLoc);
        } else {
          JavaFiles.gen_SimpleCharStream(templateLoc);
        }
      }
    }
    
    try {
      ostr = new java.io.PrintWriter(
                new java.io.BufferedWriter(
                   new java.io.FileWriter(
                     new java.io.File(Options.getOutputDirectory(), cu_name + CONSTANTS_FILENAME_SUFFIX)
                   ),
                   8192
                )
             );
    } catch (java.io.IOException e) {
      JavaCCErrors.semantic_error("Could not open file " + cu_name + "Constants.java for writing.");
      throw new Error();
    }

    List<String> tn = new ArrayList<String>(toolNames);
    tn.add(toolName);
    ostr.println("/* " + getIdString(tn, cu_name + CONSTANTS_FILENAME_SUFFIX) + " */");

    if (cu_to_insertion_point_1.size() != 0 &&
        cu_to_insertion_point_1.get(0).kind == PACKAGE
       ) {
      for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
        if (cu_to_insertion_point_1.get(i).kind == SEMICOLON) {
          printTokenSetup(cu_to_insertion_point_1.get(0));
          for (int j = 0; j <= i; j++) {
            t = cu_to_insertion_point_1.get(j);
            printToken(t, ostr, true);
          }
          printTrailingComments(t, ostr, true);
          ostr.println("");
          ostr.println("");
          break;
        }
      }
    }
    ostr.println("");
    ostr.println("/**");
    ostr.println(" * Token literal values and constants.");
    ostr.println(" * Generated by org.javacc.parser.OtherFilesGen#start()");
    ostr.println(" */");

    if(Options.getSupportClassVisibilityPublic()) {
      ostr.print("public ");
    }
    ostr.println("interface " + cu_name + "Constants {");
    ostr.println("");

    RegularExpression re;
    ostr.println("  /** End of File. */");
    ostr.println("  int EOF = 0;");
    for (java.util.Iterator<RegularExpression> it = ordered_named_tokens.iterator(); it.hasNext();) {
      re = it.next();
      ostr.println("  /** RegularExpression Id. */");
      ostr.println("  int " + re.label + " = " + re.ordinal + ";");
    }
    ostr.println("");
    if (!Options.getUserTokenManager() && Options.getBuildTokenManager()) {
      for (int i = 0; i < LexGen.lexStateName.length; i++) {
        ostr.println("  /** Lexical state. */");
        ostr.println("  int " + LexGen.lexStateName[i] + " = " + i + ";");
      }
      ostr.println("");
    }
    ostr.println("  /** Literal token values. */");
    ostr.println("  String[] tokenImage = {");
    ostr.println("    \"<EOF>\",");

    for (java.util.Iterator<TokenProduction> it = rexprlist.iterator(); it.hasNext();) {
      TokenProduction tp = it.next();
      List<RegExprSpec> respecs = tp.respecs;
      for (java.util.Iterator<RegExprSpec> it2 = respecs.iterator(); it2.hasNext();) {
        RegExprSpec res = it2.next();
        re = res.rexp;
        ostr.print("    ");
        if (re instanceof RStringLiteral) {
          ostr.println("\"\\\"" + add_escapes(add_escapes(((RStringLiteral)re).image)) + "\\\"\",");
        } else if (!re.label.equals("")) {
          ostr.println("\"<" + re.label + ">\",");
        } else {
          if (re.tpContext.kind == TokenProduction.TOKEN) {
            JavaCCErrors.warning(re, "Consider giving this non-string token a label for better error reporting.");
          }
          ostr.println("\"<token of kind " + re.ordinal + ">\",");
        }

      }
    }
    ostr.println("  };");
    ostr.println("");
    ostr.println("}");

    ostr.close();

  }

  static private java.io.PrintWriter ostr;

  public static void reInit()
  {
    ostr = null;
  }

}
