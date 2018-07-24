package com.inschos.product.taikang.access.http.controller.action;

import com.inschos.product.taikang.access.http.controller.bean.BaseResponseBean;
import com.inschos.product.taikang.access.http.controller.bean.BuyInsureBean;
import com.inschos.product.taikang.access.http.controller.bean.PayInsureBean;
import com.inschos.product.taikang.assist.kit.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import static com.inschos.product.taikang.access.http.controller.bean.CommonBean.*;
import static com.inschos.product.taikang.access.http.controller.bean.RsaEncryBean.*;

@Component
public class IntersAction extends BaseAction {

    private static final Logger logger = Logger.getLogger(IntersAction.class);

    /**
     * http请求公共函数
     *
     * @param url       请求地址
     * @param json      请求报文,格式是json
     * @param interName 接口名称
     * @return String
     */
    private BaseResponseBean httpRequest(String url, String json, String interName) {
        BaseResponseBean response = new BaseResponseBean();
        if (interName == null) {
            interName = "";
        }
        try {
            logger.info(interName+"接口请求地址："+url);
            logger.info(interName+"接口请求参数："+json);
            String result = HttpClientKit.post(url, JsonKit.bean2Json(json));
            logger.info(interName+"接口返回数据："+result);
            if (result == null) {
                return responseBean(BaseResponseBean.CODE_FAILURE, interName + "接口请求失败1", response);
            }
            response.data = result;
            return responseBean(BaseResponseBean.CODE_SUCCESS, interName + "接口请求成功", response);
        } catch (IOException e) {
            e.printStackTrace();
            return responseBean(BaseResponseBean.CODE_FAILURE, interName + "接口请求失败2", response);
        }
    }

    /**
     * 核保
     *
     * @param httpServletRequest
     * @return
     */
    public String checkInsure(HttpServletRequest httpServletRequest) {
        BuyInsureBean.Requset request = JsonKit.json2Bean(HttpKit.readRequestBody(httpServletRequest), BuyInsureBean.Requset.class);
        BaseResponseBean response = new BaseResponseBean();
        String interName = "核保";
        if (request == null) {
            return json(BaseResponseBean.CODE_FAILURE, interName + "参数解析失败", response);
        }
        BuyInsureBean.Requset buyRequest = new BuyInsureBean.Requset();
        buyRequest.orderno = request.orderno;//订单信息,订单号小于等于20位
        buyRequest.orderfrom = "";//订单来源
        buyRequest.Plancode = plancode;//产品编码
        buyRequest.orgid = orgid;//系统编码
        buyRequest.startdate = request.startdate;//生效时间
        buyRequest.applydate = request.applydate;//投保时间
        buyRequest.period = period;//保险期间
        buyRequest.premium = premium;//保费
        buyRequest.amnt = request.amnt;//保额
        //投保人信息
        buyRequest.name = request.name;
        buyRequest.cidtype = request.cidtype;
        buyRequest.cid = request.cid;
        buyRequest.sex = request.sex;//代码表见附录。
        buyRequest.birth = request.birth;//格式：yyyy-mm-dd
        buyRequest.mobile = request.mobile;
        buyRequest.email = request.email;
        //被保人信息
        buyRequest.insurerelation = request.insurerelation;//被保人与投保人关系 码表
        buyRequest.insurename = request.insurename;
        buyRequest.insurecidtype = request.insurecidtype;//被保人证件类型
        buyRequest.insurecid = request.insurecid;
        buyRequest.insuresex = request.insuresex;
        buyRequest.insurebirth = request.insurebirth;
        //受益人信息
        buyRequest.bnfrelation = request.bnfrelation;
        buyRequest.bnfname = request.bnfname;
        buyRequest.bnfcidtype = request.bnfcidtype;
        buyRequest.bnfcid = request.bnfcid;
        buyRequest.bnfsex = request.bnfsex;
        buyRequest.bnfbirth = request.bnfbirth;
        //其他信息
        buyRequest.agentno = "52589745";//业务员编码
        buyRequest.agenttype = "AG";//业务员渠道
        buyRequest.agentcomcode = "1";//业务员所属分公司
        BuyInsureBean.PolicySpldatas policySpldata = new BuyInsureBean.PolicySpldatas();
        policySpldata.psdt_value = "";
        policySpldata.psit_code = "";
        policySpldata.psit_desc = "";
        List<BuyInsureBean.PolicySpldatas> policySpldatas = new ArrayList<>();
        policySpldatas.add(policySpldata);
        buyRequest.policyspldatas = policySpldatas;
        String requestData = JsonKit.bean2Json(buyRequest);
        logger.info(interName+"请求数据："+requestData);
        String data = encryptUtil.getEncryptStr(key, requestData);
        BaseResponseBean interResponse = httpRequest(checkInsureUrl, orgid+"|"+data, interName);
        if(interResponse.code!=200){
            return json(BaseResponseBean.CODE_FAILURE, interName + "接口请求失败", response);
        }
        String result = interResponse.data.toString();
        result = encryptUtil.getDecryptStr(key, result);
        if (!isJSONValid(result)) {
            return json(BaseResponseBean.CODE_FAILURE, interName + "接口返回报文解析失败", response);
        }
        logger.info("返回数据"+JsonKit.bean2Json(interResponse));
        BuyInsureBean.Response buyResponse = new BuyInsureBean.Response();
        response.data = interResponse;
        return json(BaseResponseBean.CODE_FAILURE, "接口完善中。。。", response);
    }

    /**
     * 缴费
     *
     * @param httpServletRequest
     * @return
     */
    public String payInsure(HttpServletRequest httpServletRequest) {
        PayInsureBean.Requset request = JsonKit.json2Bean(HttpKit.readRequestBody(httpServletRequest), PayInsureBean.Requset.class);
        BaseResponseBean response = new BaseResponseBean();
        String interName = "缴费";
        if (request == null) {
            return json(BaseResponseBean.CODE_FAILURE, interName + "参数解析失败", response);
        }
        PayInsureBean.Requset payRequest = new PayInsureBean.Requset();
        payRequest.businessType = request.businessType;
        payRequest.cmsSystemSource = request.cmsSystemSource;
        payRequest.cmsBusinessType = request.cmsBusinessType;
        payRequest.cmsVersion = request.cmsVersion;
        payRequest.cmsFormat = request.cmsFormat;
        payRequest.cmsPayChannel = request.cmsPayChannel;
        payRequest.cmsPayTag = request.cmsPayTag;
        payRequest.orderId = request.orderId;
        payRequest.transactionId = request.transactionId;
        payRequest.requestDateTime = request.requestDateTime;
        payRequest.currencyCode = request.currencyCode;
        payRequest.money = request.money;
        payRequest.summary = request.summary;
        payRequest.isCheck = request.isCheck;
        payRequest.accountName = request.accountName;
        payRequest.certificateType = request.certificateType;
        payRequest.certificateNo = request.certificateNo;
        payRequest.mobile = request.mobile;
        payRequest.isLimitCreditPay = "0";
        payRequest.openId = "";
        payRequest.deviceInfo = request.deviceInfo;//应用类型
        payRequest.mchAppId = request.mchAppId;//应用标识(H5支付必填)
        payRequest.mchAppName = request.mchAppName;//应用名(H5支付必填)
        payRequest.isRaw = "";
        payRequest.successURL = request.successURL;
        payRequest.failURL = "";
        RSAUtil rsaUtil = new RSAUtil();
        ByteKit byteKit = new ByteKit();
        RSAPublicKey recoveryPubKey = rsaUtil.generateRSAPublicKey(pubModBytes, pubPubExpBytes);
        RSAPrivateKey recoveryPriKey = rsaUtil.generateRSAPrivateKey(priModBytes, priPriExpBytes);
        byte[] requestData = byteKit.toByteArray(payRequest);
        byte[] encryptData = rsaUtil.encrypt(recoveryPriKey,requestData);
        String data = new String(encryptData);
        BaseResponseBean interResponse = httpRequest(payInsureUrl, data, interName);
        logger.info("返回数据"+JsonKit.bean2Json(interResponse));
        if(interResponse.code!=200){
            return json(BaseResponseBean.CODE_FAILURE, interName + "接口请求失败", response);
        }
        Object result = interResponse.data;
        byte[] returnResponse= byteKit.toByteArray(result);
        byte[] decryptData = rsaUtil.decrypt(recoveryPubKey,returnResponse);
        Object responseData = byteKit.toObject(decryptData);
        if (responseData==null) {
            return json(BaseResponseBean.CODE_FAILURE, interName + "接口返回报文解析失败", response);
        }
        PayInsureBean.Response buyResponse = new PayInsureBean.Response();
        response.data = responseData;
        return json(BaseResponseBean.CODE_FAILURE, "接口完善中。。。", response);
    }

    /**
     * 承保
     *
     * @param httpServletRequest
     * @return
     */
    public String buyInsure(HttpServletRequest httpServletRequest) {
        BuyInsureBean.Requset request = JsonKit.json2Bean(HttpKit.readRequestBody(httpServletRequest), BuyInsureBean.Requset.class);
        BaseResponseBean response = new BaseResponseBean();
        return json(BaseResponseBean.CODE_FAILURE, "接口完善中。。。", response);
    }


    /**
     * 支付查询
     *
     * @param httpServletRequest
     * @return
     */
    public String payQuery(HttpServletRequest httpServletRequest) {
        BuyInsureBean.Requset request = JsonKit.json2Bean(HttpKit.readRequestBody(httpServletRequest), BuyInsureBean.Requset.class);
        BaseResponseBean response = new BaseResponseBean();
        return json(BaseResponseBean.CODE_FAILURE, "接口完善中。。。", response);
    }

    /**
     * 支付通知(回调)
     *
     * @param httpServletRequest
     * @return
     */
    public String payCallBack(HttpServletRequest httpServletRequest) {
        BuyInsureBean.Requset request = JsonKit.json2Bean(HttpKit.readRequestBody(httpServletRequest), BuyInsureBean.Requset.class);
        BaseResponseBean response = new BaseResponseBean();
        return json(BaseResponseBean.CODE_FAILURE, "接口完善中。。。", response);
    }


}
