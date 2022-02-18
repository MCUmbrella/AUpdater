package vip.floatationdevice.aupdater;

import static vip.floatationdevice.aupdater.Util.*;

public class AUpdater
{
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// runtime values
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static String ip, prevIP;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// config values
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // default config file path: "./aupdater.properties"
    // can be overridden by command line argument
    static String configPath ="aupdater.properties";

    static String
            login_token,
            subdomain_name,
            record_line;
    static int
            token_id,
            domain_id,
            subdomain_id,
            update_interval_minutes,
            update_interval_minutes_err;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// api related
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    final static String
            API_URL = "https://dnsapi.cn/",
            UA = "AUpdater/1.0(mcumbrella@protonmail.com)",
            LANG = "en",
            ERR_ON_EMPTY = "no";

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// main
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args)
    {
        // override default config path?
        switch(args.length)
        {
            case 0:
                break;
            case 1:
            case 2:
                configPath = args[0];
                break;
            default:
                System.err.println("Usage: java -jar AUpdater.jar [configPath] [--test]");
                System.exit(1);
        }

        // load config
        try
        {
            System.out.println("Loading config");
            loadConfig();
            System.out.println("Config loaded");
            // just a test?
            if(args.length == 2)
            {
                if(args[1].equals("--test"))
                {
                    System.out.println("=================================================="
                            + "\nConfig file test passed:"
                            + "\n  login_token = " + login_token
                            + "\n  token_id = "+ token_id
                            + "\n  domain_id = " + domain_id
                            + "\n  subdomain_id = " + subdomain_id
                            + "\n  subdomain_name = " + subdomain_name
                            + "\n  record_line = " + record_line
                            + "\n  update_interval_minutes = " + update_interval_minutes
                            + "\n  update_interval_minutes_err = " + update_interval_minutes_err
                            + "\n=================================================="
                    );
                    System.exit(0);
                }
                else
                {
                    System.err.println("Usage: java -jar AUpdater.jar [configPath] [--test]");
                    System.exit(1);
                }
            }
        }catch (Exception e)
        {
            System.err.println("Error loading config file");
            e.printStackTrace();
            System.exit(-1);
        }

        // get previous IP first
        try
        {
            System.out.println("Getting previous IP record");
            prevIP = getPreviousIP();
            System.out.println("Previous IP: " + prevIP);
        }catch (Exception e)
        {
            System.err.println("Getting previous IP address failed");
            e.printStackTrace();
            prevIP="0.0.0.0";
        }

        // run updater loop
        try
        {
            do
            {
                // get local ip
                try
                {
                    System.out.println("Getting local IP address");
                    ip = getLocalIP();
                    System.out.println("Local IP: " + ip);
                }catch(Exception e)
                {
                    System.err.println("Getting local IP address failed");
                    e.printStackTrace();
                    ip = prevIP;
                    System.out.println("Next update scheduled in "+update_interval_minutes_err+" minute(s)");
                    Thread.sleep((long)update_interval_minutes_err*60*1000);
                    continue;
                }

                // skip if ip is the same as previous
                if(ip.equals(prevIP))
                {
                    System.out.println("IP address is the same as previous one\nNext update scheduled in "+update_interval_minutes+" minute(s)");
                    Thread.sleep((long)update_interval_minutes*60*1000);
                    continue;
                }

                // update record
                try
                {
                    System.out.println("Updating record");
                    updateRecord();
                    System.out.println("Update record success: " + prevIP + " to " + ip);
                }catch(Exception e)
                {
                    System.err.println("Failed to update record");
                    e.printStackTrace();
                    System.out.println("Next update scheduled in " + update_interval_minutes_err + " minute(s)");
                    Thread.sleep((long)update_interval_minutes_err*60*1000);
                    continue;
                }

                // update previous ip, sleep for 30 minutes
                prevIP=ip;
                System.out.println("Next update scheduled in " + update_interval_minutes + " minute(s)");
                Thread.sleep((long)update_interval_minutes*60*1000);
            }while(true);
        }catch (InterruptedException e){e.printStackTrace();}
    }
}
