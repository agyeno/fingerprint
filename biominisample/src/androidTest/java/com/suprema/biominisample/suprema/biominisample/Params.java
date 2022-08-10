package com.suprema.biominisample.suprema.biominisample;

import static com.suprema.biominisample.suprema.biominisample.Utility.appendLog;

/**
 * Created by ihkim_win10_x64 on 2017-08-31.
 */
interface IPARAMS{
  public boolean isDifferent(Object obj) ;
  public void generateParams();
  public void generateRandomWithout(IPARAMS withoutParam);
}
abstract  class PARAMS {

  public int getRandom(int n){
    return (int)(Math.random()*7);
  }
  public int getRandomWithout(int nMax , int nWithout){
    int nValue = nWithout;
    do{
      nValue = getRandom(nMax);
    }while(nValue == nWithout);
    return nValue;
  }
}

class CaptureParams extends  PARAMS implements  IPARAMS{
  int Sensitivity;
  int Security_level;
  int Timeout ;

  final int MAX_SENSITIVITY = 7;
  final int MAX_SECURITY_LEVEL =7;
  final int TIMEOUT =10;

  @Override
  public boolean isDifferent(Object obj) {
    if( obj instanceof CaptureParams) {
      CaptureParams comp = (CaptureParams)obj;

      appendLog("1st param : " + this.toString());
      appendLog("2nd param : " + comp.toString());
      if( comp.Sensitivity != Sensitivity &&
          comp.Security_level != Security_level &&
          comp.Timeout != Timeout
          ) {
        return true;
      }else {
        return false;
      }
    }else{
      appendLog("Object is not a instance of CaptureParams");
      return false;
    }
  }
  @Override
  public void generateParams() {
    Sensitivity = getRandom(MAX_SENSITIVITY);
    Security_level = getRandom(MAX_SECURITY_LEVEL);
    Timeout = getRandom(TIMEOUT);
  }
  @Override
  public void generateRandomWithout(IPARAMS withoutParam) {
    this.Sensitivity =getRandomWithout(MAX_SENSITIVITY , ((CaptureParams)withoutParam).Sensitivity);
    this.Security_level =getRandomWithout(MAX_SECURITY_LEVEL , ((CaptureParams)withoutParam).Security_level);
    this.Timeout =getRandomWithout(TIMEOUT , ((CaptureParams)withoutParam).Timeout);
  }
  public CaptureParams(int sensitivity, int security_level, int timeout) {
    Sensitivity = sensitivity;
    Security_level = security_level;
    Timeout = timeout;
  }
  public CaptureParams(){
    generateParams();
  }
  @Override
  public String toString() {
    return "CaptureParams{" +
        "\nSensitivity=" + Sensitivity +
        "\n, Security_level=" + Security_level +
        "\n, Timeout=" + Timeout +
        "\n}";
  }

}