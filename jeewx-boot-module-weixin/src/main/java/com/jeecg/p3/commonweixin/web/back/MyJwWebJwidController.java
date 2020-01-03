package com.jeecg.p3.commonweixin.web.back;

import com.jeecg.p3.baseApi.util.OSSBootUtil;
import com.jeecg.p3.commonweixin.def.CommonWeixinProperties;
import com.jeecg.p3.commonweixin.entity.JwSystemUserJwidVo;
import com.jeecg.p3.commonweixin.entity.JwSystemUserVo;
import com.jeecg.p3.commonweixin.entity.MyJwWebJwid;
import com.jeecg.p3.commonweixin.exception.CommonweixinException;
import com.jeecg.p3.commonweixin.service.MyJwSystemUserService;
import com.jeecg.p3.commonweixin.util.AccessTokenUtil;
import com.jeecg.p3.commonweixin.util.Constants;
import com.jeecg.p3.open.entity.WeixinOpenAccount;
import com.jeecg.p3.open.service.WeixinOpenAccountService;
import com.jeecg.p3.redis.service.RedisService;
import com.jeecg.p3.system.service.MyJwWebJwidService;
import com.jeecg.p3.weixin.util.WxErrCodeUtil;
import com.jeecg.p3.weixinInterface.entity.WeixinAccount;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.http.entity.ContentType;
import org.apache.velocity.VelocityContext;
import org.jeecgframework.p3.core.common.utils.AjaxJson;
import org.jeecgframework.p3.core.util.SystemTools;
import org.jeecgframework.p3.core.util.plugin.ViewVelocity;
import org.jeecgframework.p3.core.utils.common.PageQuery;
import org.jeecgframework.p3.core.utils.common.StringUtils;
import org.jeecgframework.p3.core.web.BaseController;
import org.jeewx.api.core.common.WxstoreUtils;
import org.jeewx.api.third.JwThirdAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * 描述：</b>JwWebJwidController<br>微信公众号字典表
 *
 * @author pituo
 * @since：2015年12月21日 16时33分45秒 星期一
 * @version:1.0
 */
@Controller
@RequestMapping("/commonweixin/back/myJwWebJwid")
public class MyJwWebJwidController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(MyJwWebJwidController.class);

    @Autowired
    private MyJwWebJwidService myJwWebJwidService;
    @Autowired
    private WeixinOpenAccountService weixinOpenAccountService;
    @Autowired
    private MyJwSystemUserService myJwSystemUserService;

    @Autowired
    private RedisService redisService;

    /**
     * 列表页面
     *
     * @return
     */
    @RequestMapping(value = "list", method = {RequestMethod.GET, RequestMethod.POST})
    public void list(@ModelAttribute MyJwWebJwid query, HttpServletResponse response, HttpServletRequest request,
                     @RequestParam(required = false, value = "pageNo", defaultValue = "1") int pageNo,
                     @RequestParam(required = false, value = "pageSize", defaultValue = "10") int pageSize) throws Exception {
        VelocityContext velocityContext = new VelocityContext();
        String viewName = "commonweixin/back/myJwWebJwid-list.vm";
        try {
            String systemUserid = request.getSession().getAttribute("system_userid").toString();
            if (StringUtils.isEmpty(systemUserid)) {
                throw new CommonweixinException("登录人不能为空！");
            }
            query.setCreateBy(systemUserid);
            PageQuery<MyJwWebJwid> pageQuery = new PageQuery<MyJwWebJwid>();
            pageQuery.setPageNo(pageNo);
            pageQuery.setPageSize(pageSize);
            String jwid = request.getSession().getAttribute("jwid").toString();
            pageQuery.setQuery(query);
            velocityContext.put("jwid", jwid);
            velocityContext.put("myJwWebJwid", query);
            velocityContext.put("systemUserid", systemUserid);
            velocityContext.put("pageInfos", SystemTools.convertPaginatedList(myJwWebJwidService.queryPageList(pageQuery)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ViewVelocity.view(request, response, viewName, velocityContext);
    }

    /**
     * 详情
     *
     * @return
     */
    @RequestMapping(value = "toDetail", method = RequestMethod.GET)
    public void jwWebJwidDetail(@RequestParam(required = true, value = "id") String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        VelocityContext velocityContext = new VelocityContext();
        String viewName = "commonweixin/back/myJwWebJwid-detail.vm";
        MyJwWebJwid myJwWebJwid = myJwWebJwidService.queryById(id);
        velocityContext.put("myJwWebJwid", myJwWebJwid);
        String jwid = request.getSession().getAttribute("jwid").toString();
        velocityContext.put("jwid", jwid);
        ViewVelocity.view(request, response, viewName, velocityContext);
    }

    /**
     * 跳转到添加页面
     *
     * @return
     */
    @RequestMapping(value = "/toAdd", method = {RequestMethod.GET, RequestMethod.POST})
    public void toAddDialog(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        VelocityContext velocityContext = new VelocityContext();
        String viewName = "commonweixin/back/myJwWebJwid-add.vm";
        String jwid = request.getSession().getAttribute("jwid").toString();
        velocityContext.put("jwid", jwid);
        ViewVelocity.view(request, response, viewName, velocityContext);
    }

    /**
     * 保存信息
     *
     * @return
     */
    @RequestMapping(value = "/doAdd", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public AjaxJson doAdd(@ModelAttribute MyJwWebJwid myJwWebJwid, HttpServletRequest request) {
        AjaxJson j = new AjaxJson();
        try {
            //先验证用户是否已经创建
            String createBy = (String) request.getSession().getAttribute(Constants.SYSTEM_USERID);
            if (!"admin".equals(createBy)) {
                MyJwWebJwid jwWebJwid = myJwWebJwidService.queryOneByCreateBy(createBy);
                if (jwWebJwid != null) {
                    j.setSuccess(false);
                    j.setMsg("每个用户只能创建一个公共号!");
                    return j;
                }
            }

            myJwWebJwid.setAuthType("1");
            Map<String, Object> map = AccessTokenUtil.getAccseeToken(myJwWebJwid.getWeixinAppId(), myJwWebJwid.getWeixinAppSecret());
            if (map.get("accessToken") != null) {
                myJwWebJwid.setAccessToken(map.get("accessToken").toString());
                myJwWebJwid.setTokenGetTime((Date) map.get("accessTokenTime"));
                myJwWebJwid.setApiTicket(map.get("apiTicket").toString());
                myJwWebJwid.setApiTicketTime((Date) map.get("apiTicketTime"));
                myJwWebJwid.setJsApiTicket(map.get("jsApiTicket").toString());
                myJwWebJwid.setJsApiTicketTime((Date) map.get("jsApiTicketTime"));
                j.setMsg("公众号授权成功");

                WeixinAccount po = new WeixinAccount();
                po.setAccountappid(myJwWebJwid.getWeixinAppId());
                po.setAccountappsecret(myJwWebJwid.getWeixinAppSecret());
                po.setAccountaccesstoken(myJwWebJwid.getAccessToken());
                po.setAddtoekntime(myJwWebJwid.getTokenGetTime());
                po.setAccountnumber(myJwWebJwid.getWeixinNumber());
                po.setApiticket(myJwWebJwid.getApiTicket());
                po.setApiticketttime(myJwWebJwid.getApiTicketTime());
                po.setAccounttype(myJwWebJwid.getAccountType());
                po.setWeixinAccountid(myJwWebJwid.getJwid());//原始ID
                po.setJsapiticket(myJwWebJwid.getJsApiTicket());
                po.setJsapitickettime(myJwWebJwid.getJsApiTicketTime());
                try {
					redisService.set(myJwWebJwid.getWeixinAppId(),po);
                } catch (Exception e) {
                    log.error(e.toString());
                }
            } else {
                //update-begin--Author:zhangweijian  Date: 20181112 for：白名单提示优化问题
                if (map.get("errcode").equals("40164")) {
                    j.setMsg(WxErrCodeUtil.ERROR_40164 + "&nbsp;&nbsp;<a target='_blank' href='http://www.h5huodong.com/h5/detail.html?id=ff80808165e062030165e6451e6d1d58'>操作指南</a>");
                } else {
                    j.setMsg("AppId或 AppSecret配置不正确，请注意检查。 ");
                }
                //update-end--Author:zhangweijian  Date: 20181112 for：白名单提示优化问题
                //update-begin--Author:zhangweijian  Date: 20180910 for：配置不成功，不能保存
                j.setSuccess(false);
                return j;
                //update-end--Author:zhangweijian  Date: 20180910 for：配置不成功，不能保存
            }
            myJwWebJwid.setCreateBy(createBy);
            MyJwWebJwid myJwWebJwid2 = myJwWebJwidService.queryByJwid(myJwWebJwid.getJwid());
            if (myJwWebJwid2 != null) {
                j.setSuccess(false);
                j.setMsg("该微信公众号已存在!");
                return j;
            }
            myJwWebJwidService.doAdd(myJwWebJwid);
            request.getSession().setAttribute(Constants.SYSTEM_JWID, myJwWebJwid.getJwid());
            request.getSession().setAttribute(Constants.SYSTEM_JWIDNAME, myJwWebJwid.getName());
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            j.setSuccess(false);
            j.setMsg("保存失败");
        }
        return j;
    }

    /**
     * 跳转到编辑页面
     *
     * @return
     */
    @RequestMapping(value = "toEdit", method = RequestMethod.GET)
    public void toEdit(@RequestParam(required = true, value = "id") String id, HttpServletResponse response, HttpServletRequest request) throws Exception {
        VelocityContext velocityContext = new VelocityContext();
        MyJwWebJwid myJwWebJwid = myJwWebJwidService.queryById(id);
        velocityContext.put("myJwWebJwid", myJwWebJwid);
        String viewName = "commonweixin/back/myJwWebJwid-edit.vm";
        String jwid = request.getSession().getAttribute("jwid").toString();
        velocityContext.put("jwid", jwid);
        ViewVelocity.view(request, response, viewName, velocityContext);
    }

    /**
     * 编辑
     *
     * @return
     */
    @RequestMapping(value = "/doEdit", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public AjaxJson doEdit(@ModelAttribute MyJwWebJwid myJwWebJwid, @RequestParam(required = true, value = "oldjwid") String oldjwid, HttpServletRequest request) {
        log.info("------------------------------公众号编辑操作---------------------------");
        AjaxJson j = new AjaxJson();
        try {
            //update-begin--Author:zhangweijian Date:20181011 for：扫码登陆不限制密钥
            String authType = myJwWebJwid.getAuthType();
            if (!authType.equals("2")) {
                log.info("------------------------------AccessTokenUtil.getAccseeToken--------begin-------------------");
                Map<String, Object> map = AccessTokenUtil.getAccseeToken(myJwWebJwid.getWeixinAppId(), myJwWebJwid.getWeixinAppSecret());
                log.info("------------------------------AccessTokenUtil.getAccseeToken--------end-------------------");
                if (map.get("accessToken") != null) {
                    myJwWebJwid.setAccessToken(map.get("accessToken").toString());
                    myJwWebJwid.setTokenGetTime((Date) map.get("accessTokenTime"));
                    myJwWebJwid.setApiTicket(map.get("apiTicket").toString());
                    myJwWebJwid.setApiTicketTime((Date) map.get("apiTicketTime"));
                    myJwWebJwid.setJsApiTicket(map.get("jsApiTicket").toString());
                    myJwWebJwid.setJsApiTicketTime((Date) map.get("jsApiTicketTime"));

                    if (!oldjwid.equals(myJwWebJwid.getJwid())) {
                        //开启线程，同步变更所有业务表公众号原始ID
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                myJwWebJwidService.switchDefaultOfficialAcco(myJwWebJwid.getId(), oldjwid, myJwWebJwid.getJwid());
                            }
                        });
                        t.start();

                        //5.判断登录公众号ID
                        Object cache_jwid = request.getSession().getAttribute(Constants.SYSTEM_JWID);
                        if (cache_jwid != null && cache_jwid.toString().equals(oldjwid)) {
                            request.getSession().setAttribute(Constants.SYSTEM_JWID, myJwWebJwid.getJwid());
                        }
                    }

                    WeixinAccount po = new WeixinAccount();
                    po.setAccountappid(myJwWebJwid.getWeixinAppId());
                    po.setAccountappsecret(myJwWebJwid.getWeixinAppSecret());
                    po.setAccountaccesstoken(myJwWebJwid.getAccessToken());
                    po.setAddtoekntime(myJwWebJwid.getTokenGetTime());
                    po.setAccountnumber(myJwWebJwid.getWeixinNumber());
                    po.setApiticket(myJwWebJwid.getApiTicket());
                    po.setApiticketttime(myJwWebJwid.getApiTicketTime());
                    po.setAccounttype(myJwWebJwid.getAccountType());
                    po.setWeixinAccountid(myJwWebJwid.getJwid());//原始ID
                    po.setJsapiticket(myJwWebJwid.getJsApiTicket());
                    po.setJsapitickettime(myJwWebJwid.getJsApiTicketTime());
                    try {
                        log.info("--------------------redisService-------------setWxAccount-------------------" + myJwWebJwid.toString());
						redisService.set(myJwWebJwid.getWeixinAppId(),po);
                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                } else {
                    //update-begin--Author:zhangweijian  Date: 20181112 for：白名单提示优化问题
                    if (map.get("errcode").equals("40164")) {
                        j.setMsg(WxErrCodeUtil.ERROR_40164 + "&nbsp;&nbsp;<a target='_blank' href='http://www.h5huodong.com/h5/detail.html?id=ff80808165e062030165e6451e6d1d58'>操作指南</a>");
                    } else {
                        j.setMsg("AppId或 AppSecret配置不正确，请注意检查。 ");
                    }
                    //update-end--Author:zhangweijian  Date: 20181112 for：白名单提示优化问题
                    //update-begin--Author:zhangweijian  Date: 20180910 for：配置不成功，不能保存
                    j.setSuccess(false);
                    return j;
                    //update-end--Author:zhangweijian  Date: 20180910 for：配置不成功，不能保存
                }
            }
            myJwWebJwidService.doEdit(myJwWebJwid);
            j.setMsg("公众号授权成功");
            //update-end--Author:zhangweijian Date:20181011 for：扫码登陆不限制密钥
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            j.setSuccess(false);
            j.setMsg("编辑失败");
        }
        return j;
    }


    /**
     * 删除
     *
     * @return
     */
    @RequestMapping(value = "doDelete", method = RequestMethod.GET)
    @ResponseBody
    public AjaxJson doDelete(@RequestParam(required = true, value = "id") String id) {
        AjaxJson j = new AjaxJson();
        try {
            myJwWebJwidService.doDelete(id);
            j.setMsg("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            j.setSuccess(false);
            j.setMsg("删除失败");
        }
        return j;
    }

    /**
     * 重置 AccessToken
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "reset", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public AjaxJson resetAccessToken(@RequestParam(required = true, value = "id") String id) {
        AjaxJson json = new AjaxJson();
        try {
            log.info("------------------resetAccessToken------------------");
            String resetAccessToken = myJwWebJwidService.resetAccessToken(id);
            if (StringUtils.isNotEmpty(resetAccessToken)) {
                if ("success".equals(resetAccessToken)) {
                    json.setMsg("重置token成功");
                } else {
                    json.setSuccess(false);
                    json.setMsg("重置token失败：" + resetAccessToken);
                }
            } else {
                json.setSuccess(false);
                json.setMsg("重置token失败：系统异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            json.setSuccess(false);
            json.setMsg("重置token失败：系统异常");
        }
        return json;
    }

    /**
     * 保存图片
     *
     * @return
     */
    @RequestMapping(value = "/doUpload", method = {RequestMethod.POST})
    @ResponseBody
    public AjaxJson doUpload(MultipartHttpServletRequest request, HttpServletResponse response) {
        AjaxJson j = new AjaxJson();
        try {
            MultipartFile uploadify = request.getFile("file");
        /*byte[] bytes = uploadify.getBytes();
        String realFilename=uploadify.getOriginalFilename();
        String fileExtension = realFilename.substring(realFilename.lastIndexOf("."));
        String filename=UUID.randomUUID().toString().replace("-", "")+fileExtension;
        //String uploadDir = request.getSession().getServletContext().getRealPath("upload/img/commonweixin/");
        String uploadDir = upLoadPath + "/upload/img/commonweixin/";
        File dirPath = new File(uploadDir);
        if (!dirPath.exists()) {  
            dirPath.mkdirs();  
        }  
        String sep = System.getProperty("file.separator");  
        File uploadedFile = new File(uploadDir + sep  + filename);  
        FileCopyUtils.copy(bytes, uploadedFile);*/
            String filename = OSSBootUtil.upload(uploadify, "upload/img/commonweixin");
            j.setObj(filename);
            j.setSuccess(true);
            j.setMsg("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            j.setSuccess(false);
            j.setMsg("保存失败");
        }
        return j;
    }

    /**
     * 去扫码授权页面（扫描授权公众号）
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "toSweepCodeAuthorization", method = {RequestMethod.GET, RequestMethod.POST})
    public void toSweepCodeAuthorization(HttpServletRequest request, HttpServletResponse response) throws Exception {
        VelocityContext velocityContext = new VelocityContext();
        String viewName = "open/back/myJwWebJwid-sweepCodeAuthorization.vm";
        ViewVelocity.view(request, response, viewName, velocityContext);
    }

    /**
     * 扫描授权公众号
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getAuthhorizationUrl")
    public AjaxJson getAuthhorizationUrl(HttpServletRequest request) {
        AjaxJson j = new AjaxJson();
        try {
            String url = CommonWeixinProperties.authhorizationUrl;
            WeixinOpenAccount weixinOpenAccount = weixinOpenAccountService.queryOneByAppid(CommonWeixinProperties.component_appid);
            if (weixinOpenAccount == null) {
                throw new CommonweixinException("通过APPID获取WEIXINOPENACCOUNT为空!");
            }
            //获取ACCESSTOKEN
            if (StringUtils.isEmpty(weixinOpenAccount.getComponentAccessToken())) {
                throw new CommonweixinException("未获取到第三方平台的ACCESSTOKEN");
            }
            //获取预授权码
            String preAuthCode = JwThirdAPI.getPreAuthCode(CommonWeixinProperties.component_appid, weixinOpenAccount.getComponentAccessToken());
            url = url.replace("PRE_AUTH_CODE", preAuthCode);
            String redirect_uri = URLEncoder.encode(CommonWeixinProperties.authhorizationCallBackUrl + "?userId=" + request.getSession().getAttribute(Constants.SYSTEM_USERID), "UTF-8");
            url = url.replace("REDIRECT_URI", redirect_uri).replace("COMPONENT_APPID", CommonWeixinProperties.component_appid);
            log.info("===========拼接访问授权页面地址===地址为===" + url + "============");
            j.setObj(url);
        } catch (CommonweixinException e) {
            e.printStackTrace();
            j.setMsg("系统异常，请稍后再试!");
            j.setSuccess(false);
            log.error("getAuthhorizationUrl error={}", new Object[]{e.getMessage()});
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getAuthhorizationUrl error={}", new Object[]{e});
            j.setMsg("系统异常，请稍后再试!");
            j.setSuccess(false);
        }
        return j;
    }

    /**
     * 授权回调地址
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "callback", method = {RequestMethod.GET, RequestMethod.POST})
    public void callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String message = "授权成功！";
        VelocityContext velocityContext = new VelocityContext();
        String viewName = "open/back/myJwWebJwid-callback.vm";
        velocityContext.put("message", message);
        try {
            String authCode = request.getParameter("auth_code");
            WeixinOpenAccount weixinOpenAccount = weixinOpenAccountService.queryOneByAppid(CommonWeixinProperties.component_appid);

            //调取接口获取平台ACCESSTOKEN
            String componentAccessToken = weixinOpenAccount.getComponentAccessToken();
            if (StringUtils.isEmpty(componentAccessToken)) {
                throw new CommonweixinException("授权公共号回调时获取ACCESSTOKEN为空!");
            }

            //调取接口
            String urlFormat = CommonWeixinProperties.getApiQueryAuth.replace("COMPONENT_ACCESS_TOKEN", componentAccessToken);
            JSONObject json = new JSONObject();
            json.put("component_appid", CommonWeixinProperties.component_appid);
            json.put("authorization_code", authCode);
            log.info("授权公共号回调后调取接口请求参数为：{}", new Object[]{json.toString()});
            JSONObject jsonObject = WxstoreUtils.httpRequest(urlFormat, "POST", json.toString());
            log.info("授权公共号回调后调取接口返回参数为：{}", new Object[]{jsonObject});
            if (jsonObject != null && !jsonObject.containsKey("errcode")) {
                MyJwWebJwid myJwWebJwid = new MyJwWebJwid();
                // 保存授权公众号的部分信息
                myJwWebJwid.setCreateBy(request.getParameter("userId"));
                save(jsonObject, myJwWebJwid);
                // 通过第三方token获取公众号信息
                String getAuthorizerInfoUrl = CommonWeixinProperties.getAuthorizerInfo.replace("COMPONENT_ACCESS_TOKEN", componentAccessToken);
                JSONObject j = new JSONObject();
                // 第三方平台appid
                j.put("component_appid", CommonWeixinProperties.component_appid);
                // 授权用户的appid
                j.put("authorizer_appid", myJwWebJwid.getWeixinAppId());
                JSONObject jsonObj = WxstoreUtils.httpRequest(getAuthorizerInfoUrl, "POST", j.toString());
                log.info("===========授权回调方法===获取授权公众号详细Info===" + jsonObj.toString() + "===========");
                if (jsonObj != null && !jsonObj.containsKey("errcode")) {
                    // 增加授权返回标识，已授权的提示用户更新成功！
                    callbackUpdate(jsonObj, myJwWebJwid);
                }
            }
        } catch (CommonweixinException e) {
            e.printStackTrace();
            message = "授权失败";
            log.error("授权信息回调方法中，发生错误，错误信息={}", new Object[]{e.getMessage()});

        } catch (Exception e) {
            e.printStackTrace();
            log.error("授权信息回调方法中，发生错误，错误信息={}", new Object[]{e});
            message = "授权失败";
        }
	/*PrintWriter pw = null;
	try {
		//response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		pw = response.getWriter();
		pw.write("<h2 style='text-align:center;color:#FEA128;'>"+message+"</h2>");
		pw.write("<h3 style='text-align:center;color:#FEA128;'>请自行关闭当前页面</h3>");
		pw.flush();
	} finally{
		pw.close();
	}*/
        ViewVelocity.view(request, response, viewName, velocityContext);
    }

    /**
     * 更新内容
     *
     * @param jsonObj
     * @param myJwWebJwid
     */
    private void callbackUpdate(JSONObject jsonObj, MyJwWebJwid myJwWebJwid) {
        try {
            String authorizerInfoStr = jsonObj.getString("authorizer_info");
            String qrcodeUrl = null;
            JSONObject authorizerInfoJson = JSONObject.fromObject(authorizerInfoStr);
            if (authorizerInfoJson.containsKey("qrcode_url")) {
                qrcodeUrl = authorizerInfoJson.getString("qrcode_url");
            }
            String nickName = authorizerInfoJson.getString("nick_name");
            String headImg = null;
            if (authorizerInfoJson.containsKey("head_img") && StringUtils.isNotEmpty(authorizerInfoJson.getString("head_img"))) {
                headImg = authorizerInfoJson.getString("head_img");
                myJwWebJwid.setHeadimgurl(headImg);
            }
            String serviceTypeInfo = authorizerInfoJson.getString("service_type_info");
            String verifyTypeInfo = authorizerInfoJson.getString("verify_type_info");
            String userName = authorizerInfoJson.getString("user_name");
            String businessInfo = authorizerInfoJson.getString("business_info");
            String alias = "";
            if (authorizerInfoJson.containsKey("alias")) {
                alias = authorizerInfoJson.getString("alias");
            }
            String authorizationInfoS = jsonObj.getString("authorization_info");
            JSONObject authorization_info_json = JSONObject.fromObject(authorizationInfoS);
            String func_info = authorization_info_json.getString("func_info");
            myJwWebJwid.setWeixinNumber(alias);
            myJwWebJwid.setBusinessInfo(businessInfo);
            myJwWebJwid.setFuncInfo(func_info);
            myJwWebJwid.setName(nickName);
            String fileName = UUID.randomUUID().toString().replace("-", "").toUpperCase() + ".jpg";
            String uploadDir = "upload/img/commonweixin";
            //update-begin--Author:zhaofei  Date: 20191016 for：将微信返回的二维码链接上传云服务器
            MultipartFile multipartFile = createFileItem(qrcodeUrl, fileName);
            String fileNames = OSSBootUtil.upload(multipartFile, uploadDir);
            //update-end--Author:zhaofei  Date: 20191016 for：将微信返回的二维码链接上传云服务器
            //download(qrcodeUrl, fileName, uploadDir);
            myJwWebJwid.setQrcodeimg(fileNames);
            JSONObject json = JSONObject.fromObject(serviceTypeInfo);
            if (json != null && json.containsKey("id")) {
                int accountType = json.getInt("id");
                if (2 == accountType) {
                    myJwWebJwid.setAccountType("1");
                } else {
                    myJwWebJwid.setAccountType("2");
                }
            }
            json = JSONObject.fromObject(verifyTypeInfo);
            if (json != null && json.containsKey("id")) {
                int authStatus = json.getInt("id");
                if (authStatus == -1) {
                    myJwWebJwid.setAuthStatus("0");
                } else {
                    myJwWebJwid.setAuthStatus("1");
                }
            }
            myJwWebJwid.setJwid(userName);
            //获取apiticket
            Map<String, String> apiTicket = AccessTokenUtil.getApiTicket(myJwWebJwid.getAccessToken());
            if ("true".equals(apiTicket.get("status"))) {
                myJwWebJwid.setApiTicket(apiTicket.get("apiTicket"));
                myJwWebJwid.setApiTicketTime(new Date());
                myJwWebJwid.setJsApiTicket(apiTicket.get("jsApiTicket"));
                myJwWebJwid.setJsApiTicketTime(new Date());
            }
            //TODO 没有授权就新增，授权过就修改
            MyJwWebJwid webJwid = myJwWebJwidService.queryByJwid(userName);
            if (webJwid == null) {
                myJwWebJwidService.doAdd(myJwWebJwid);
            } else {
                myJwWebJwid.setId(webJwid.getId());
                myJwWebJwidService.doUpdate(myJwWebJwid);
            }
            //-------H5平台独立公众号，重置redis缓存,将token写到redis-------------------------------------------
            try {
                log.info("----------定时任务：H5平台独立公众号，重置redis缓存token开始-------------");
                WeixinAccount po = new WeixinAccount();
                po.setAccountappid(myJwWebJwid.getWeixinAppId());
                po.setAccountappsecret(myJwWebJwid.getWeixinAppSecret());
                po.setAccountaccesstoken(myJwWebJwid.getAccessToken());
                po.setAddtoekntime(myJwWebJwid.getTokenGetTime());
                po.setAccountnumber(myJwWebJwid.getWeixinNumber());
                po.setApiticket(myJwWebJwid.getApiTicket());
                po.setApiticketttime(myJwWebJwid.getApiTicketTime());
                po.setAccounttype(myJwWebJwid.getAccountType());
                po.setWeixinAccountid(myJwWebJwid.getJwid());//原始ID
                po.setJsapiticket(myJwWebJwid.getJsApiTicket());
                po.setJsapitickettime(myJwWebJwid.getJsApiTicketTime());
				redisService.set(myJwWebJwid.getWeixinAppId(),po);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("----------定时任务：H5平台独立公众号，重置redis缓存token失败-------------" + e.toString());
            }
            //--------H5平台独立公众号，重置redis缓存---------------------------------------
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonweixinException("解析授权信息==UPDATE时发生错误" + e.getMessage());
        }
    }

    /**
     * 保存内容
     *
     * @param jsonObject
     * @param myJwWebJwid
     */
    private void save(JSONObject jsonObject, MyJwWebJwid myJwWebJwid) {
        try {
            String authorizationInfoStr = jsonObject.getString("authorization_info");
            JSONObject authorizationInfoJson = JSONObject.fromObject(authorizationInfoStr);
            String authorizerAppid = null;
            if (authorizationInfoJson.containsKey("authorizer_appid")) {
                authorizerAppid = authorizationInfoJson.getString("authorizer_appid");
            } else if (jsonObject.containsKey("authorizer_appid")) {
                authorizerAppid = jsonObject.getString("authorizer_appid");
            }
            String authorizerAccessToken = authorizationInfoJson.getString("authorizer_access_token");
            String authorizerRefreshToken = authorizationInfoJson.getString("authorizer_refresh_token");
            String funcInfoStr = "";
            if (authorizationInfoJson.containsKey("func_info")) {
                funcInfoStr = authorizationInfoJson.getString("func_info");
            } else if (jsonObject.containsKey("func_info")) {
                funcInfoStr = jsonObject.getString("func_info");
            }
            myJwWebJwid.setAuthorizationInfo(authorizationInfoStr);
            myJwWebJwid.setAccessToken(authorizerAccessToken);
            myJwWebJwid.setTokenGetTime(new Date());
            myJwWebJwid.setWeixinAppId(authorizerAppid);
            myJwWebJwid.setAuthorizerRefreshToken(authorizerRefreshToken);
            myJwWebJwid.setFuncInfo(funcInfoStr);
            myJwWebJwid.setAuthType("2");
            //授权状态（1正常，2已取消）
            myJwWebJwid.setAuthorizationStatus("1");
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonweixinException("解析授权信息==DOADD时发生错误" + e.getMessage());
        }

    }

    /**
     * @param urlString
     * @param filename
     * @param savePath
     * @throws IOException
     */
    private void download(String urlString, String filename, String savePath) throws IOException {
        OutputStream os = null;
        InputStream is = null;
        try {
            log.info("授权公共号的二维图片地址为：{},保存的文件名：{},保存的路径{}", new Object[]{urlString, filename, savePath});
            // 构造URL
            URL url = new URL(urlString);
            // 打开连接
            URLConnection con = url.openConnection();
            // 输入流
            is = con.getInputStream();
            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 输出的文件流
            String sep = System.getProperty("file.separator");
            os = new FileOutputStream(savePath + sep + filename);
            // 开始读取
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("============下载图片时出现错误============,error={}", e);
        } finally {
            if (os != null) {
                os.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

//update-begin-zhangweijian-----Date:20180808---for:变更公众号原始ID

    /**
     * @param response
     * @param request
     * @throws Exception
     * @功能：进入变更公众号原始ID页面
     * @author zhangweijian
     */
    @RequestMapping(value = "toSwitchDefaultOfficialAcco", method = RequestMethod.GET)
    public void toSwitchDefaultOfficialAcco(@RequestParam String jwid, HttpServletResponse response, HttpServletRequest request) throws Exception {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("jwid", jwid);
        String viewName = "commonweixin/back/switchDefaultOfficialAcco.vm";
        ViewVelocity.view(request, response, viewName, velocityContext);
    }

    /**
     * @param jwid
     * @return
     * @功能:变更公众号原始ID
     * @作者:liwenhui
     * @时间:2018-3-15 下午01:59:14
     * @修改：
     */
    @RequestMapping(value = "switchDefaultOfficialAcco", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public AjaxJson switchDefaultOfficialAcco(@RequestParam final String jwid, @RequestParam final String newJwid) {
        AjaxJson j = new AjaxJson();
        try {
            MyJwWebJwid oldJwid = myJwWebJwidService.queryByJwid(newJwid);
            if (oldJwid != null) {
                j.setMsg("该公众号原始ID已存在");
                j.setSuccess(false);
                return j;
            }
            //开启线程，同步粉丝数据
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    myJwWebJwidService.switchDefaultOfficialAcco(oldJwid.getId(), jwid, newJwid);
                }
            });
            t.start();
            j.setMsg("变更公众号原始ID已启动,请稍侯刷新");
            j.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            j.setMsg("操作失败");
            j.setSuccess(false);
        }
        return j;
    }
//update-end-zhangweijian-----Date:20180808---for:变更公众号原始ID

    //update-begin--Author:zhangweijian Date:20181019 for：授权公众号管理员

    /**
     * @功能：查询待授权管理员
     */
    @RequestMapping(value = "searchManager", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public AjaxJson searchManager(@RequestParam String phone) {
        AjaxJson j = new AjaxJson();
        try {
            List<JwSystemUserVo> jwSystemUser = myJwSystemUserService.queryByPhone(phone);
            if (jwSystemUser.size() > 0) {
                j.setObj(jwSystemUser.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return j;
    }

    /**
     * @param userId
     * @return
     * @功能：授权管理员权限
     */
    @RequestMapping(value = "authManager", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public AjaxJson authManager(@RequestParam String userId, @RequestParam String jwid, HttpServletRequest request) {
        AjaxJson j = new AjaxJson();
        try {
            JwSystemUserJwidVo jwSystemUserJwid = new JwSystemUserJwidVo();
            jwSystemUserJwid.setUserId(userId);
            jwSystemUserJwid.setJwid(jwid);
            myJwSystemUserService.authManager(jwSystemUserJwid);
            j.setSuccess(true);
            j.setMsg("授权成功！");
            j.setObj(jwSystemUserJwid.getId());
        } catch (Exception e) {
            e.printStackTrace();
            j.setSuccess(false);
            j.setMsg("当前管理员已授权");
        }
        return j;
    }

    /**
     * @param request
     * @return
     * @功能：获取已授权管理员信息
     */
    @RequestMapping(value = "getManager", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public AjaxJson getManager(HttpServletRequest request, @RequestParam String jwid) {
        AjaxJson j = new AjaxJson();
        try {
            List<JwSystemUserJwidVo> jwSystemUserJwid = myJwSystemUserService.queryByJwid(jwid);
            if (jwSystemUserJwid.size() > 0) {
                j.setObj(jwSystemUserJwid);
            } else {
                j.setObj("");
            }
            j.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return j;
    }

    /**
     * @功能：取消授权
     */
    @RequestMapping(value = "cancelAuth", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public AjaxJson cancelAuth(HttpServletRequest request, @RequestParam String id) {
        AjaxJson j = new AjaxJson();
        try {
            myJwSystemUserService.deleteById(id);
            j.setSuccess(true);
            j.setMsg("取消成功");
        } catch (Exception e) {
            e.printStackTrace();
            j.setMsg("取消失败");
        }
        return j;
    }
    //update-end--Author:zhangweijian Date:20181019 for：授权公众号管理员

    /**
     * url转变为 MultipartFile对象
     *
     * @param url
     * @param fileName
     * @return
     * @throws Exception
     */
    private static MultipartFile createFileItem(String url, String fileName) throws Exception {
        FileItem item = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            //设置应用程序要从网络连接读取数据
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();

                FileItemFactory factory = new DiskFileItemFactory(16, null);
                String textFieldName = "uploadfile";
                item = factory.createItem(textFieldName, ContentType.APPLICATION_OCTET_STREAM.toString(), false, fileName);
                OutputStream os = item.getOutputStream();

                int bytesRead = 0;
                byte[] buffer = new byte[8192];
                while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                is.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }

        return new CommonsMultipartFile(item);
    }
}
