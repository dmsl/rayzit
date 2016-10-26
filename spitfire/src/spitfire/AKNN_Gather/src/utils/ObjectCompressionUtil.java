/**
 * Copyright (c) 2016 Data Management Systems Laboratory, University of Cyprus
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/
/**
 * Created by Constantinos Costa.
 * Copyright (c) 2014 DMSL. All rights reserved
 */
package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
 
/**
 * The Class ObjectCompressionUtil.
 * 
 * @param <T> the generic type of the serializable object to be compressed
 */
public class ObjectCompressionUtil<T extends Serializable> {
 
 /**
  * Compress object.
  * 
  * @param objectToCompress the object to compress
  * @param outstream the outstream
  * @return the compressed object
  * @throws IOException Signals that an I/O exception has occurred.
  */
 public T compressObject(final T objectToCompress, final OutputStream outstream) throws IOException {
 
  final GZIPOutputStream gz = new GZIPOutputStream(outstream);
  final ObjectOutputStream oos = new ObjectOutputStream(gz);
   
  try {
  oos.writeObject(objectToCompress);
  oos.flush();
  return objectToCompress;
  }finally {
   oos.close();
   outstream.close();
  }
 
 }
 
 /**
  * Expand object.
  * 
  * @param objectToExpand the object to expand
  * @param instream the instream
  * @return the expanded object
  * @throws IOException Signals that an I/O exception has occurred.
  * @throws ClassNotFoundException the class not found exception
  */
 public T expandObject(final T objectToExpand, final InputStream instream) throws IOException,
   ClassNotFoundException {
  final GZIPInputStream gs = new GZIPInputStream(instream);
  final ObjectInputStream ois = new ObjectInputStream(gs);
 
  try {
   @SuppressWarnings("unchecked")
   T expandedObject = (T) ois.readObject();
   return expandedObject;
  } finally {
   gs.close();
   ois.close();
  }
 }
 
}