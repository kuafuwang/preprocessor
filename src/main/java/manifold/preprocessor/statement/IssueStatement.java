/*
 * Copyright (c) 2019 - Manifold Systems LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package manifold.preprocessor.statement;


import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import manifold.internal.javac.IDynamicJdk;
import manifold.internal.javac.JavacPlugin;
import manifold.preprocessor.Problem;
import manifold.preprocessor.TokenType;
import manifold.preprocessor.definitions.Definitions;
import manifold.preprocessor.expression.StringLiteral;

public class IssueStatement extends Statement
{
  private final int _tokenStart;
  private final StringLiteral _message;
  private final boolean _isError;

  public IssueStatement(int start, int end, StringLiteral message, boolean isError  , Consumer<Problem> issueConsumer)
  {
    super( TokenType.Undef, start, end );
    _tokenStart = start;
    _message = message;
    _isError = isError;
    _issueConsumer = issueConsumer;
  }
  private Consumer<Problem> _issueConsumer;
  @Override
  public void execute( StringBuilder result, CharSequence source, boolean visible, Definitions definitions)
  {
    preserveMaskedOutSpace( result, source );
    if(null != _issueConsumer){
       int severity = _isError ? Problem.Error: Problem.Warning;
       Problem p = new Problem(_message.getValue( definitions ),severity,_tokenStart,getTokenStart());
      _issueConsumer.accept( p);
    }
    if( !visible )
    {
      return;
    }

    //## todo: handle _isError, add logWarning()

    if( JavacPlugin.instance() != null )
    {
      IDynamicJdk.instance().logError( Log.instance( JavacPlugin.instance().getContext() ),
              new JCDiagnostic.SimpleDiagnosticPosition( _tokenStart ),
              "proc.messager", _message.getValue( definitions ) );
    }
  }

  @Override
  public void execute( List<SourceStatement> result, boolean visible, Definitions definitions )
  {
  }
  @Override
  public void execute_for_no_visible(List<SourceStatement> result, boolean visible, Definitions definitions )
  {
  }
  @Override
  public boolean hasPreprocessorDirectives()
  {
    return true;
  }
}
