

package org.glob3.mobile.specific;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glob3.mobile.generated.ILogger;
import org.glob3.mobile.generated.LogLevel;

import com.google.gwt.regexp.shared.RegExp;


public final class Logger_WebGL
   extends
      ILogger {

   private final Logger _logger;


   public Logger_WebGL(final LogLevel level) {
      super(level);

      _logger = Logger.getLogger("");
   }


   @Override
   public void logInfo(final String x,
                       final Object... LegacyParamArray) {
      if (_level.getValue() <= LogLevel.InfoLevel.getValue()) {
         final String res = stringFormat(x, LegacyParamArray);
         _logger.log(Level.INFO, res);
      }
   }


   @Override
   public void logWarning(final String x,
                          final Object... LegacyParamArray) {
      if (_level.getValue() <= LogLevel.WarningLevel.getValue()) {
         final String res = stringFormat(x, LegacyParamArray);
         _logger.log(Level.WARNING, res);
      }
   }


   @Override
   public void logError(final String x,
                        final Object... LegacyParamArray) {
      if (_level.getValue() <= LogLevel.ErrorLevel.getValue()) {
         final String res = stringFormat(x, LegacyParamArray);
         _logger.log(Level.SEVERE, res);
      }
   }


   static public String stringFormat(final String format,
                                     final Object... args) {
      final RegExp exp = RegExp.compile("%[sdf]");
      int nextSub = 0;
      String output = "";
      for (int i = 0; i < exp.split(format).length(); i++) {
         output = output + exp.split(format).get(i);
         if (((i + 1) < exp.split(format).length()) && (nextSub < args.length)) {
            output = output + args[nextSub];
         }
         nextSub++;
      }

      return output;
   }


}
