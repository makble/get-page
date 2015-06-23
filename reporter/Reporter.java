package reporter;

public class Reporter {

    public String reportLevel;
    public final int levelCommon = 4;

    public Reporter() {
        reportLevel = "1info";
    }

    public Reporter(String level) {
        reportLevel = level;
    }

    private void ErrorPrimitive(String errormsg,int level) {
        if ( !isNotSuppressed("4error") ) return;
        System.out.println("ERROR: " + getContextInfoWithLevel(level) + errormsg);
    }

    public void Error(String errormsg) {
        ErrorPrimitive(errormsg,levelCommon);
    }
    
    public void error(String errormsg) {
        ErrorPrimitive(errormsg,levelCommon);
    }

    public void err(String errormsg) {
        ErrorPrimitive(errormsg,levelCommon);
    }

    public void e(String errormsg) {
        ErrorPrimitive(errormsg,levelCommon);
    }
    
    private void WarningPrimitive(String warnmsg, int level) {
        if ( !isNotSuppressed("3warning") ) return;
        System.out.println("WARNING: " + getContextInfoWithLevel(level) + warnmsg);    
    }

    public void Warning(String warnmsg) {
        WarningPrimitive(warnmsg, levelCommon);
    }
    public void warning(String warnmsg) {
        WarningPrimitive(warnmsg, levelCommon);
    }

    public void warn(String warnmsg) {
        WarningPrimitive(warnmsg, levelCommon);
    }

    private void DebugPrimitive(String debugmsg,int level) {
        if ( !isNotSuppressed("2debug") ) return;

        System.out.println("DEBUG: " + getContextInfoWithLevel(level) + debugmsg);
    }

    public void Debug(String debugmsg) {
        DebugPrimitive(debugmsg, levelCommon);
    }

    public void debug(String debugmsg) {
        DebugPrimitive(debugmsg, levelCommon);
    }


    public void d(String debugmsg) {
        DebugPrimitive(debugmsg, levelCommon);
    }
    
    private void InfoPrimitive(String msg,int level) {
        if ( !isNotSuppressed("1info") ) return;
        System.out.println("INFO: " + msg);
    }

    public void Info(String msg) {
        InfoPrimitive(msg, levelCommon);
    }

    public void info(String msg) {
        InfoPrimitive(msg, levelCommon);
    }
    
    public String getContextInfoWithLevel(int level) {
        
        String fullClassName = Thread.currentThread().getStackTrace()[level].getClassName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[level].getMethodName();
        int lineNumber = Thread.currentThread().getStackTrace()[level].getLineNumber();

        String contextInfo = className + "." + methodName + "():" + lineNumber + ", ";
        return contextInfo;
    }
    
    public boolean isNotSuppressed(String level) {
        if (level.compareTo(reportLevel) >= 0) {
            return true;
        } else {
            return false;
        }
    }    
}
