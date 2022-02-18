package vip.floatationdevice.aupdater;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

import static vip.floatationdevice.aupdater.AUpdater.*;

public class Util
{
    static void checkNullArguments(Object... objects)
    {
        for(Object o : objects)
            if(o == null || o.toString().isEmpty())
                throw new IllegalArgumentException("Null value detected");
    }

    static void loadConfig() throws Exception
    {
        BufferedReader f = new BufferedReader(new FileReader(configPath));
        Properties p = new Properties();
        p.load(f);
        f.close();
        login_token = p.getProperty("login_token");
        token_id = Integer.parseUnsignedInt(p.getProperty("token_id"));
        domain_id = Integer.parseUnsignedInt(p.getProperty("domain_id"));
        subdomain_id = Integer.parseUnsignedInt(p.getProperty("subdomain_id"));
        subdomain_name = p.getProperty("subdomain_name");
        record_line = p.getProperty("record_line");
        update_interval_minutes = Integer.parseUnsignedInt(p.getProperty("update_interval_minutes"));
        update_interval_minutes_err = Integer.parseUnsignedInt(p.getProperty("update_interval_minutes_err"));
        checkNullArguments(
                login_token,
                token_id,
                domain_id,
                subdomain_id,
                subdomain_name,
                record_line,
                update_interval_minutes,
                update_interval_minutes_err
        );
        if(update_interval_minutes == 0 || update_interval_minutes_err == 0)
            throw new IllegalArgumentException("Interval must >0");
    }

    static String getGeneralArgs()
    {
        return "login_token=" + token_id + "," + login_token + "&format=json&lang=" + LANG + "&err_on_empty=" + ERR_ON_EMPTY;
    }

    static String getLocalIP()
    {
        String result = HttpRequest.get("http://pv.sohu.com/cityjson?ie=utf-8").execute().body();
        String t = result.substring(result.indexOf("{"),result.lastIndexOf("}")+1);
        if(JSONUtil.isJson(t))
        {
            String cip = JSONUtil.parseObj(t).getStr("cip");
            if(cip == null) throw new NullPointerException("No IP address found in JSON string:\n" + t);
            return cip;
        }
        else throw new ClassCastException("Result is not JSON string:\n" + t);
    }

    static String getPreviousIP()
    {
        String result = HttpRequest.post(API_URL + "Record.Info")
                .clearHeaders()
                .header("User-Agent", UA)
                .header("Accept", "application/json")
                .body(getGeneralArgs()
                        +"&domain_id=" + domain_id
                        +"&record_id=" + subdomain_id
                        +"&remark=\"\""
                ).execute().body();
        if(JSONUtil.isJson(result))
        {
            JSONObject json = JSONUtil.parseObj(result);
            int statusCode = Integer.parseInt(json.getByPath("status.code").toString());
            if(statusCode == 1) return json.getByPath("record.value").toString();
            else throw new IllegalStateException("API status code is not 1:\n" + result);
        }
        else throw new ClassCastException("Result is not JSON string:\n" + result);
    }

    static void updateRecord() throws IORuntimeException
    {
        String result = HttpRequest.post(API_URL + "Record.Modify")
                .clearHeaders()
                .header("User-Agent", UA)
                .header("Accept", "application/json")
                .body(getGeneralArgs()
                        +"&domain_id=" + domain_id
                        +"&record_id=" + subdomain_id
                        +"&sub_domain=" + subdomain_name
                        +"&record_type=A"
                        +"&value=" + ip
                        +"&record_line=" + record_line
                        +"&mx=1"
                ).execute().body();
        if(JSONUtil.isJson(result))
        {
            if (Integer.parseInt(JSONUtil.parseObj(result).getByPath("status.code").toString()) != 1)
                throw new IllegalStateException("API status code is not 1:\n" + result);
        }
        else throw new IllegalStateException("Result is not JSON string:\n" + result);
    }
}
