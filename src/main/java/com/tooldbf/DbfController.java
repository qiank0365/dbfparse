package com.tooldbf;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by qk on 2017/6/28.
 */
// application 注解
@Configuration
@ComponentScan
@EnableAutoConfiguration
@PropertySource(value = "classpath:/config/dbf.properties", ignoreResourceNotFound = true)
// Controller 注解
@Controller
public class DbfController {
    @Value("${dbf.inputPath}")
    private String inputPath;

    @Value("${dbf.outputPath}")
    private String outputPath;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "index";
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public String config(ModelMap modelMap) {
        return "config";
    }

    @RequestMapping(value = "/path", method = RequestMethod.GET)
    public String path(ModelMap modelMap) {
        return "path";
    }

    @RequestMapping(value = "/setDBFCfg", method = RequestMethod.POST)
    @ResponseBody
    public String setDBFCfg(@RequestParam("action") String action,
                            @RequestBody String data) {
        String ret = "";
        JSONObject dataJson = new JSONObject(getJsonFile());// 创建一个包含原始json串的json对象
        JSONArray array = dataJson.getJSONArray("data");

        try {
            String decode = URLDecoder.decode(data, "UTF-8");
            String[] datalist = decode.split("&");
            String[] data0 = datalist[0].split("=");
            String dataaction = data0[1];
            if (dataaction.equals("create")) {
                String[] data1 = datalist[1].split("=");
                String[] data2 = datalist[2].split("=");
                String[] data3 = datalist[3].split("=");
                JSONObject newdata = new JSONObject();
                newdata.put("code", data1[1]);
                newdata.put("name", data2[1]);
                newdata.put("field", data3[1]);
                newdata.put("DT_RowId", UUID.randomUUID().toString());
                array.put(newdata);

                JSONObject retJson = new JSONObject();
                JSONArray retarray = new JSONArray();
                retarray.put(newdata);
                retJson.put("data",retarray);
                ret = retJson.toString();
            } else if (dataaction.equals("remove")) {
                String[] data4 = datalist[4].split("=");
                for (int i = 0;i < array.length();i++){
                    JSONObject ob = (JSONObject) array.get(i);
                    if (ob.getString("DT_RowId").equals(data4[1])) {
                        array.remove(i);
                        break;
                    }
                }
                ret = dataJson.toString();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        setJsonFile(dataJson.toString());

        return ret;
    }

    @RequestMapping(value = "/setSrcPath", method = RequestMethod.POST)
    @ResponseBody
    public String setSrcPath(@RequestParam(required = true) String srcPath) {
        inputPath = srcPath;
        return "";
    }

    @RequestMapping(value = "/getSrcPath", method = RequestMethod.GET)
    @ResponseBody
    public String getSrcPath() {
        return inputPath;
    }

    @RequestMapping(value = "/setDstPath", method = RequestMethod.POST)
    @ResponseBody
    public String setDstPath(@RequestParam(required = true) String dstPath) {
        outputPath = dstPath;
        return "";
    }

    @RequestMapping(value = "/getDstPath", method = RequestMethod.GET)
    @ResponseBody
    public String getDstPath() {
        return outputPath;
    }

    @RequestMapping(value = "/code", method = RequestMethod.POST)
    @ResponseBody
    public String code(){
        ArrayList aList = new ArrayList();

        JSONObject dataJson = new JSONObject(getJsonFile());// 创建一个包含原始json串的json对象
        JSONArray array = dataJson.getJSONArray("data");
        for (int i = 0;i < array.length();i++){
            JSONObject ob = (JSONObject) array.get(i);
            String code = (String) ob.get("code");
            if (i == 0 || !aList.contains(code)) {
                aList.add(code);
            }

        }
        String ret = aList.toString().replace("[","");
        ret = ret.replace("]","");

        return ret;
    }

    @RequestMapping(value = "/file", method = RequestMethod.POST)
    @ResponseBody
    public String file(@RequestParam(required = true) String code){
        ArrayList aList = new ArrayList();

        JSONObject dataJson = new JSONObject(getJsonFile());// 创建一个包含原始json串的json对象
        JSONArray array = dataJson.getJSONArray("data");
        for (int i = 0;i < array.length();i++){
            JSONObject ob = (JSONObject) array.get(i);
            String tempcode = (String) ob.get("code");
            if (tempcode.equals(code)) {
                String tempname = (String) ob.get("name");
                aList.add(tempname);
            }
        }
        String ret = aList.toString().replace("[","");
        ret = ret.replace("]","");
        return ret;
    }

    @RequestMapping(value="selectDBF", method = RequestMethod.POST)
    @ResponseBody
    public void selectDBF (HttpServletRequest request,
                           HttpServletResponse response,
                           @RequestParam(required = true) String selectCode,
                           @RequestParam(required = false) String selectFile
                           ) {
        InputStream fis = null;
        OutputStream fos = null;

        // 读取文件的输入流
        String key = "";
        JSONObject dataJson = new JSONObject(getJsonFile());// 创建一个包含原始json串的json对象
        JSONArray array = dataJson.getJSONArray("data");
        for (int i = 0;i < array.length();i++){
            JSONObject ob = (JSONObject) array.get(i);
            String tempcode = (String) ob.get("code");
            if (tempcode.equals(selectCode)) {
                key = (String) ob.get("field");
                break;
            }
        }
        Integer pos = 0;
        String[] fileList = selectFile.split(",");
        for (Integer index = 0; index < fileList.length; index++) {
            try {
                fis = new FileInputStream(inputPath + fileList[index]);
                // 根据输入流初始化一个DBFReader实例，用来读取DBF文件信息
                DBFReader reader = new DBFReader(fis);
                // 调用DBFReader对实例方法得到path文件中字段的个数
                int fieldsCount = reader.getFieldCount();
                ArrayList<DBFField> fieldList = new ArrayList<DBFField>();
                // 取出字段信息
                for (int i = 0; i < fieldsCount; i++) {
                    DBFField field = reader.getField(i);
                    if (field.getName().equals(key)) {
                        pos = i;
                    }
                    fieldList.add(field);
                }

                DBFWriter writer = new DBFWriter();
                // 把字段信息写入DBFWriter实例，即定义表结构
                writer.setFields((DBFField[])fieldList.toArray(new DBFField[fieldList.size()]));

                Object[] rowValues;
                // 一条条取出path文件中记录
                while ((rowValues = reader.nextRecord()) != null) {
                    String posvalue = (String)rowValues[pos];
                    if (posvalue.replace(" ","").equals(selectCode)) {
                        writer.addRecord(rowValues);
                    }
                }

                // 定义输出流，并关联的一个文件
                String[] filename = fileList[index].split("\\.");
                String outfilename = outputPath + filename[0] + "_" + selectCode + ".DBF";
                fos = new FileOutputStream(outfilename);
                // 写入数据
                writer.write(fos);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fis.close();
                    fos.close();
                } catch (Exception e) {
                }
            }
        }

    }

    private String getJsonFile() {
        String classpath = this.getClass().getResource("/").getFile();
        String filename = classpath + "/static/dbf.json";
        File file = new File(filename);
        Scanner scanner = null;
        StringBuilder buffer = new StringBuilder();
        try {
            scanner = new Scanner(file, "utf-8");
            while (scanner.hasNextLine()) {
                buffer.append(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return buffer.toString();
    }

    private void setJsonFile(String jsonString) {
        String classpath = this.getClass().getResource("/").getFile();
        String filename = classpath + "/static/dbf.json";

        FileWriter fw = null;
        try {
            fw = new FileWriter(filename);
            PrintWriter out = new PrintWriter(fw);
            out.write(jsonString);
            out.println();
            fw.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
