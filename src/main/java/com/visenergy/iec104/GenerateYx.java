package com.visenergy.iec104;

import com.visenergy.iec104.util.ChangeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Fuxudong on 2017/11/7.
 */
public class GenerateYx {
    private static Log log = LogFactory.getLog(GenerateDate.class);

    public static String getYxData() {
        StringBuffer sBuffer = new StringBuffer("68 16 00 00 00 00 01 03 03 00 01 00");

        //NO101
        String NO101_VERSION_FAIL = ChangeUtils.encodeInfomationAddress(1);
        log.info("NO101_VERSION_FAIL：" + NO101_VERSION_FAIL);
        sBuffer.append(" " + NO101_VERSION_FAIL).append("01");
        String NO101_SYSTEM_FAIL = ChangeUtils.encodeInfomationAddress(3);
        log.info("NO101_SYSTEM_FAIL：" + NO101_SYSTEM_FAIL);
        sBuffer.append(" " + NO101_SYSTEM_FAIL).append("00");
        String NO101_WDGG_FAIL = ChangeUtils.encodeInfomationAddress(8);
        log.info("NO101_WDGG_FAIL：" + NO101_WDGG_FAIL);
        sBuffer.append(" " + NO101_WDGG_FAIL).append("01");

        return sBuffer.toString();
    }
}
