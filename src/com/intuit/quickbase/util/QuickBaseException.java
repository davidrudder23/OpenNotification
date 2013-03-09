//Exception class that encapsulates the QuickBase error code.
//version 1.3 relocated from $cvsroot/quickbase to $cvsroot/platform, 8/29/2002.
//$Id: QuickBaseException.java,v 1.1 2002/08/29 17:36:43 charlie Exp $

package com.intuit.quickbase.util;

/**
 * <p>QuickBaseException class allows users of the 
 * <a href="http://developer.intuit.com/quickbase/tools/QuickBaseAPI.html">
 * QuickBase HTTP API</a> to receive QuickBase error codes and messages.
 * Please refer to Appendix A for a complete list of error codes and messages. 
 * <p>Copyright (C) 2001 Intuit Inc. All Rights Reserved. Use is subject to <a href="http://developer.intuit.com/legal/IPRNotice_021201.html">IP Rights Notice and Restrictions</a>.  
 */
public class QuickBaseException extends Exception {

  private int errCode = 0;

  public QuickBaseException(){}

  public QuickBaseException(String msg){
    super(msg);
    }

  public QuickBaseException(String msg, String strErrCode){
    super(msg);
    errCode = Integer.parseInt(strErrCode);
    }

  /**
   * Retrieves the QuickBase error code. Please refer to
   * <a href="http://developer.intuit.com/quickbase/tools/QuickBaseAPI.html">
   * QuickBase HTTP API</a> Appendix A to get a listing of QuickBase error codes and messages.
   * 
   *
   */
  public int getErrorCode() {
    return  errCode;
  }

}

