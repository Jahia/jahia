package org.jahia.services.render.scripting;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.config.ConfigException;
import com.caucho.quercus.servlet.QuercusServlet;
import com.caucho.quercus.servlet.QuercusServletImpl;

public class JahiaQuercusServlet extends QuercusServlet {
    private static final Logger log
    = Logger.getLogger(QuercusServlet.class.getName());
    
    public JahiaQuercusServlet() {
        super();
    }

    protected QuercusServletImpl getQuercusServlet(boolean isResin)
    {
      QuercusServletImpl impl = null;

      if (isResin) {
        try {
          Class cl = Class.forName(
              "com.caucho.quercus.servlet.ProResinQuercusServlet");

          Constructor cons = cl.getConstructor(File.class);

          impl = (QuercusServletImpl) cons.newInstance(_licenseDirectory);

          //impl = (QuercusServletImpl) cl.newInstance();
        } catch (ConfigException e) {
          log.log(Level.FINEST, e.toString(), e);
          log.info("Quercus compiled mode requires Resin "
              + "personal or professional licenses");
          log.info(e.getMessage());

        } catch (Exception e) {
          log.log(Level.FINEST, e.toString(), e);
        }

        if (impl == null) {
          try {
            Class cl = Class.forName(
                "com.caucho.quercus.servlet.ResinQuercusServlet");
            impl = (QuercusServletImpl) cl.newInstance();
          } catch (Exception e) {
            log.log(Level.FINEST, e.toString(), e);
          }
        }
      }

      if (impl == null) {
        try {
          Class cl = Class.forName(
              "com.caucho.quercus.servlet.ProQuercusServlet");

          Constructor cons = cl.getConstructor(java.io.File.class);

          impl = (QuercusServletImpl) cons.newInstance(_licenseDirectory);

          //impl = (QuercusServletImpl) cl.newInstance();
        } catch (ConfigException e) {
          log.log(Level.FINEST, e.toString(), e);
          log.info("Quercus compiled mode requires "
              + "valid Quercus professional licenses");
          log.info(e.getMessage());

        } catch (Exception e) {
          log.log(Level.FINEST, e.toString(), e);
        }
      }

      if (impl == null)
        impl = new JahiaQuercusServletImpl();

      return impl;
    }
}
