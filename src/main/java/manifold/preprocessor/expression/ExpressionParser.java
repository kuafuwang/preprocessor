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

package manifold.preprocessor.expression;

import manifold.preprocessor.Problem;

import java.util.function.Consumer;

public class ExpressionParser
{
  private final CharSequence _buffer;
  private   int _relate_start;
  private final int _offset;
  private final int _endOffset;
  private ExpressionTokenizer _tokenizer;
  private final Consumer<Problem> _issueConsumer ;
  public ExpressionParser( int directive_start,CharSequence buffer, int offset, int endOffset , Consumer<Problem>  issueConsumer)
  {
    _relate_start = directive_start;
    _buffer = buffer;
    _offset = offset;
    _endOffset = endOffset;
    _issueConsumer = issueConsumer;
  }

  public Expression parse()
  {
    _tokenizer = new ExpressionTokenizer( _buffer, _offset, _endOffset );
    _tokenizer.advance();
    return parseExpression();
  }

  private Expression parseExpression()
  {
    return parseOrExpression();
  }

  private Expression parseOrExpression()
  {
    int offset = skipWhitespace();

    Expression lhs = parseAndExpression();
    while( true )
    {
      if( match( ExpressionTokenType.Or ) )
      {
        Expression rhs = parseAndExpression();
        lhs = new OrExpression( lhs, rhs, offset, rhs.getEndOffset() );
      }
      else
      {
        break;
      }
    }
    return lhs;
  }

  private Expression parseAndExpression()
  {
    int offset = skipWhitespace();

    Expression lhs = parseEqualityExpression();
    while( true )
    {
      if( match( ExpressionTokenType.And ) )
      {
        Expression rhs = parseEqualityExpression();
        lhs = new AndExpression( lhs, rhs, offset, rhs.getEndOffset() );
      }
      else
      {
        break;
      }
    }
    return lhs;
  }

  private Expression parseEqualityExpression()
  {
    int offset = skipWhitespace();

    Expression lhs = parseRelationalExpression();
    while( true )
    {
      boolean not = false;
      if( match( ExpressionTokenType.Equals ) ||
          (not = match( ExpressionTokenType.NotEquals )) )
      {
        Expression rhs = parseRelationalExpression();
        lhs = new EqualityExpression( lhs, rhs, not, offset, rhs.getEndOffset() );
      }
      else
      {
        break;
      }
    }
    return lhs;
  }

  private Expression parseRelationalExpression()
  {
    int offset = skipWhitespace();

    Expression lhs = parseUnaryExpression();
    while( true )
    {
      skipWhitespace();
      ExpressionTokenType tokenType = _tokenizer.getTokenType();
      if( match( ExpressionTokenType.gt ) ||
          match( ExpressionTokenType.ge ) ||
          match( ExpressionTokenType.lt ) ||
          match( ExpressionTokenType.le ) )
      {
        Expression rhs = parseUnaryExpression();
        lhs = new RelationalExpression( lhs, rhs, tokenType.getToken(), offset, rhs.getEndOffset(),_issueConsumer );
      }
      else
      {
        break;
      }
    }
    return lhs;
  }

  private Expression parseUnaryExpression()
  {
    int offset = skipWhitespace();

    if( match( ExpressionTokenType.Not ) )
    {
      Expression expr = parseExpression();
      return new NotExpression( expr, offset, expr.getEndOffset() );
    }
    else if( match( ExpressionTokenType.OpenParen ) )
    {
      int open_paren_offset= _tokenizer.getTokenStart();
      Expression expr = parseExpression();
      int endOffset = expr.getEndOffset();
      if( !match( ExpressionTokenType.CloseParen, false ) )
      {
        expr.error( "')' Expected", open_paren_offset,endOffset );
      }
      else
      {
        endOffset = _tokenizer.getTokenEnd();
        match( ExpressionTokenType.CloseParen );
      }
      return new ParenthesizedExpression( expr, offset, endOffset );
    }
    else if( match( ExpressionTokenType.Identifier, false ) )
    {
      int endOffset = _tokenizer.getTokenEnd();
      String name = _tokenizer.getTokenText().toString();
      match( ExpressionTokenType.Identifier );
      return new Identifier( name, offset, endOffset );
    }
    else if( match( ExpressionTokenType.StringLiteral, false ) )
    {
      int endOffset = _tokenizer.getTokenEnd();
      String string = _tokenizer.getTokenText().toString();
      match( ExpressionTokenType.StringLiteral );
      StringLiteral stringLiteral = new StringLiteral( string, offset, endOffset );
      if( string.charAt( string.length()-1 ) != '"' )
      {
        stringLiteral.error( "Missing closing quote for string literal", stringLiteral.getEndOffset()-1 ,stringLiteral.getEndOffset()-1);
      }
      return stringLiteral;
    }
    return new EmptyExpression(_relate_start, offset );
  }

  private boolean match( ExpressionTokenType type )
  {
    return match( type, true );

  }
  private boolean match( ExpressionTokenType type, boolean advance )
  {
    skipWhitespace();

    if( _tokenizer.getTokenType() == type )
    {
      _relate_start = _tokenizer.getTokenStart();
      if( advance )
      {
        _tokenizer.advance();
      }
      return true;
    }
    return false;
  }

  private int skipWhitespace()
  {
    while( _tokenizer.getTokenType() == ExpressionTokenType.Whitespace )
    {
      _tokenizer.advance();
    }
    return _tokenizer.getTokenStart();
  }
}
