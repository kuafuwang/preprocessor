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

package manifold.preprocessor;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import manifold.api.fs.IFile;
import manifold.api.fs.IFileSystem;
import manifold.api.fs.def.JavaDirectoryImpl;
import manifold.api.type.IPreprocessor;
import manifold.internal.host.JavacManifoldHost;
import manifold.preprocessor.definitions.Definitions;
import manifold.preprocessor.statement.FileStatement;
import manifold.preprocessor.statement.SourceStatement;

public class JavaPreprocessor implements IPreprocessor
{
  @Override
  public Order getPreferredOrder()
  {
    return Order.First;
  }

  @Override
  public CharSequence process( URI sourceFile, CharSequence source )
  {
    return process( sourceFile, source, null ,null);
  }
  public CharSequence process(
          URI sourceFile, CharSequence source,
          Consumer<Problem> issueConsumer,Consumer<Tokenizer> consumer
  )
  {
    PreprocessorParser  preprocessorParser = new  PreprocessorParser( source, consumer,issueConsumer );
    FileStatement fileStmt = preprocessorParser.parseFile();
    if( fileStmt.hasPreprocessorDirectives() )
    {

      StringBuilder result = new StringBuilder();
      try
      {
        IFile file ;
        if(sourceFile != null){
          JavacManifoldHost host = new JavacManifoldHost();
          file =  host.getFileSystem().getIFile( sourceFile.toURL() );
        }
        else{
          file = null;
       }

        Definitions definitions = new Definitions( file );
        fileStmt.execute( result, source, true,  definitions);
        if(!definitions.has_build_properties){
          throw  new AssertionError(file.getPath().toString() + result.toString());
        }
        return result;
      }
      catch( Exception e )
      {
        throw new IllegalStateException( e );
      }
    }
    return source;
  }




  public static int binarySearch(List<Map.Entry<Integer,Integer>> arr,int value) {
    int low=0;
    int high=arr.size()-1;
    while(low<=high) {
      int mid=(low+high)/2;
      if(value==arr.get(mid).getValue()) {
        return mid;
      }
      if(value>arr.get(mid).getValue()) {
        low=mid+1;
      }
      if(value<arr.get(mid).getValue()) {
        high=mid-1;
      }

    }
    return -1;//没有找到返回-1
  }
  public static class Node<K,V> implements Map.Entry<K,V> {

    final K key;
    V value;

    Node( K key, V v) {

      this.key = key;
      this.value = v;
    }

    public final K getKey()        { return key; }
    public final V getValue()      { return value; }
    public final String toString() { return key + "=" + value; }

    public final int hashCode() {
      return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    public final V setValue(V newValue) {
      V oldValue = value;
      value = newValue;
      return oldValue;
    }

    public final boolean equals(Object o) {
      if (o == this)
        return true;
      if (o instanceof Map.Entry) {
        Map.Entry<?,?> e = (Map.Entry<?,?>)o;
        if (Objects.equals(key, e.getKey()) &&
                Objects.equals(value, e.getValue()))
          return true;
      }
      return false;
    }
  }

  public PreprocessorResult process_for_info(URI sourceFile, CharSequence source , Consumer<Problem> issueConsumer) {
    PreprocessorResult info = new PreprocessorResult();

    try {
      PreprocessorParser preprocessorParser = new PreprocessorParser(source, null, issueConsumer);
      FileStatement fileStmt = preprocessorParser.parseFile();
      IFile file;
      if (sourceFile != null) {
        JavacManifoldHost host = new JavacManifoldHost();
        file = host.getFileSystem().getIFile(sourceFile.toURL());
      } else {
        file = null;
      }

      Definitions definitions = new Definitions(file);
      if (fileStmt.hasPreprocessorDirectives()) {
        info.hasPreprocessorDirectives = true;
        StringBuilder out_text = new StringBuilder();
        fileStmt.execute(out_text, source, true, new Definitions(file));
        info.text = out_text.toString();

        List<SourceStatement> result = new ArrayList<>();
        fileStmt.execute_for_no_visible(result, true, definitions);

        List<Map.Entry<Integer, Integer>> _preprocessor_info = new ArrayList<>();
        for (SourceStatement s : result) {
          int _s = s.getTokenStart();
          int _e = s.getTokenEnd();
          // System.out.println(String.format("%d,%d",_s,_e));
          int index = binarySearch(_preprocessor_info, _s);
          if (-1 == index)
            _preprocessor_info.add(new Node(_s, _e));
          else {
            _preprocessor_info.get(index).setValue(_e);
          }
        }
        info.position_infos =_preprocessor_info;
      } else {
        info.hasPreprocessorDirectives = false;
      }

      Map<String, String> _all_defines = definitions.getAllDefines();

      _all_defines.forEach((key, value) -> {
        info.defines.add(key);
      });
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return info;
  }
}
