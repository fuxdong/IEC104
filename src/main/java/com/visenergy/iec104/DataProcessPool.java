package com.visenergy.iec104;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.flying.jdbc.SqlHelper;
import com.flying.jdbc.SqlHelperByDruid;
import com.flying.jdbc.data.CommandType;
import com.flying.jdbc.util.DBConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhonghuan on 2017/7/25.
 */
public class DataProcessPool {
    private static Log log = LogFactory.getLog(DataProcessPool.class);

    public static Map<String,YcObject> ycPool = new HashMap<String,YcObject>();
    public static Map<String,YxObject> yxPool = new HashMap<String,YxObject>();
    public static Map<String,YxStatusObject> yxsPool = new HashMap<String,YxStatusObject>();


    public static void initPool(){
        log.debug("初始化对象池对象");
        DruidPooledConnection conn = null;
        try {
            conn = Init.connPool.getConnection();
            //查询所有逆变器的序列号、ID、所属楼宇ID
            String sql = "SELECT A.INVERTER_ID,A.SERIAL,A.BUILDING_ID FROM T_PVMANAGE_INVERTER A";
            List<Map> list = SqlHelperByDruid.executeQuery(conn,CommandType.Text,sql);
            for (int i = 0; i< list.size();i++){
                log.debug("创建逆变器" + list.get(i).get("SERIAL") + "遥信、遥测对象。");
                ycPool.put((String) list.get(i).get("SERIAL"),new YcObject((String) list.get(i).get("INVERTER_ID"),(String) list.get(i).get("SERIAL")));
                yxPool.put((String) list.get(i).get("SERIAL"),new YxObject((String) list.get(i).get("INVERTER_ID"),(String) list.get(i).get("BUILDING_ID"), (String) list.get(i).get("SERIAL")));
                yxsPool.put((String) list.get(i).get("SERIAL"),new YxStatusObject((String) list.get(i).get("INVERTER_ID"),(String) list.get(i).get("SERIAL")));
            }
        } catch (Exception e) {
           log.error("查询数据出错", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("关闭数据库连接出错", e);
                }
            }
        }
    }

}
