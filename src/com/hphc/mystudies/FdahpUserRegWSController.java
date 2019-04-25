/*
 * Copyright © 2017-2018 Harvard Pilgrim Health Care Institute (HPHCI) and its Contributors.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * Funding Source: Food and Drug Administration ("Funding Agency") effective 18 September 2014 as Contract no.
 * HHSF22320140030I/HHSF22301006T (the "Prime Contract").
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hphc.mystudies;
import com.hphc.mystudies.bean.ActivitiesBean;
import com.hphc.mystudies.bean.ConsentBean;
import com.hphc.mystudies.bean.InfoBean;
import com.hphc.mystudies.bean.NotificationBean;
import com.hphc.mystudies.bean.ParticipantForm;
import com.hphc.mystudies.bean.ParticipantInfoBean;
import com.hphc.mystudies.bean.ProfileBean;
import com.hphc.mystudies.bean.SettingsBean;
import com.hphc.mystudies.bean.StudiesBean;
import com.hphc.mystudies.model.AuthInfo;
import com.hphc.mystudies.model.FdahpUserRegUtil;
import com.hphc.mystudies.model.LoginAttempts;
import com.hphc.mystudies.model.ParticipantActivities;
import com.hphc.mystudies.model.ParticipantStudies;
import com.hphc.mystudies.model.PasswordHistory;
import com.hphc.mystudies.model.StudyConsent;
import com.hphc.mystudies.model.UserDetails;
import com.hphc.mystudies.bean.ActivitiesBean;
import com.hphc.mystudies.bean.ConsentBean;
import com.hphc.mystudies.bean.InfoBean;
import com.hphc.mystudies.bean.NotificationBean;
import com.hphc.mystudies.bean.ParticipantForm;
import com.hphc.mystudies.bean.ParticipantInfoBean;
import com.hphc.mystudies.bean.ProfileBean;
import com.hphc.mystudies.bean.SettingsBean;
import com.hphc.mystudies.bean.StudiesBean;
import com.hphc.mystudies.model.AuthInfo;
import com.hphc.mystudies.model.FdahpUserRegUtil;
import com.hphc.mystudies.model.LoginAttempts;
import com.hphc.mystudies.model.ParticipantActivities;
import com.hphc.mystudies.model.ParticipantStudies;
import com.hphc.mystudies.model.PasswordHistory;
import com.hphc.mystudies.model.StudyConsent;
import com.hphc.mystudies.model.UserDetails;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.action.ApiAction;
import org.labkey.api.action.ApiResponse;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.action.ReturnUrlForm;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.security.RequiresNoPermission;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.labkey.api.util.GUID;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.labkey.api.module.Module;
import org.labkey.api.settings.LookAndFeelProperties;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.security.CSRF;
import org.labkey.api.services.ServiceRegistry;
import org.labkey.api.files.FileContentService;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.menu.FolderAdminMenu;
import org.labkey.api.util.FileUtil;
import org.labkey.api.module.ModuleProperty;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.Container;
import java.util.Set;
public class FdahpUserRegWSController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(FdahpUserRegWSController.class);
    public static final String NAME = "fdahpuserregws";

    public FdahpUserRegWSController()
    {
        setActionResolver(_actionResolver);
    }

    private static final Logger _log = Logger.getLogger(FdahpUserRegWSController.class);

    Properties configProp = FdahpUserRegUtil.getProperties();

    @RequiresNoPermission
    public class BeginAction extends SimpleViewAction
    {
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            return new JspView("/com/hphc/mystudies/view/hello.jsp");
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }

    /**
     * Check the status of the application
     */
    @RequiresNoPermission
    public class PingAction extends ApiAction<Object>
    {

        @Override
        public ApiResponse execute(Object o, BindException errors) throws Exception
        {
            UserDetails participantDetails = new UserDetails();
            ApiSimpleResponse apiSimpleResponse = new ApiSimpleResponse();
            apiSimpleResponse.put("reponse", "FdahpUserRegWebServices-1.25 Works!");
            apiSimpleResponse.put(FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase(), true);
            return apiSimpleResponse;
        }
    }

    /**
     * Signup of an user
     *
     * */
    @RequiresNoPermission
    @CSRF(CSRF.Method.NONE)
    public class RegisterAction extends ApiAction<ParticipantForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public Object execute(ParticipantForm participantForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            UserDetails addParticipantDetails=null;
            try{
                if((participantForm.getEmailId() != null && StringUtils.isNotEmpty(participantForm.getEmailId())) && (participantForm.getPassword() != null && StringUtils.isNotEmpty(participantForm.getPassword()))){
                    List<UserDetails> participantDetails = FdahpUserRegWSManager.get().getParticipantDetailsListByEmail(participantForm.getEmailId());
                    if(participantDetails != null && participantDetails.size() > 0){
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(), FdahpUserRegUtil.ErrorCodes.EMAIL_EXISTS.getValue(), getViewContext().getResponse());
                        return null;
                    }else{
                        addParticipantDetails = FdahpUserRegWSManager.get().saveParticipant(getParticipant(participantForm));
                        if(addParticipantDetails != null){
                            response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(), FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                            response.put("userId", addParticipantDetails.getUserId());
                            if(addParticipantDetails.getStatus() != null)
                                if(addParticipantDetails.getStatus() == 2)
                                    response.put("verified", false);
                                if(addParticipantDetails.getStatus() == 1)
                                    response.put("verified", true);
                            if(addParticipantDetails.getId() != null)
                            {
                                AuthInfo authInfo = FdahpUserRegWSManager.get().saveAuthInfo(addParticipantDetails.getUserId(),true);
                                if(authInfo != null){
                                    response.put("auth", authInfo.getAuthKey());
                                    response.put("refreshToken",authInfo.getRefreshToken());
                                }
                            }
                            String message = "<html>" +
                                    "<body>" +
                                    "<div style='margin:20px;padding:10px;font-family: sans-serif;font-size: 14px;'>" +
                                    "<span>Hi,</span><br/><br/>" +
                                    "<span>Thank you for registering with us! We look forward to having you on board and actively taking part in<br/>research studies conducted by the FDA and its partners.</span><br/><br/>" +
                                    "<span>Your sign-up process is almost complete. Please use the verification code provided below to<br/>complete the Verification step in the mobile app. </span><br/><br/>" +
                                    "<span><strong>Verification Code:</strong>" +addParticipantDetails.getSecurityToken()+ "</span><br/><br/>" +
                                    "<span>This code can be used only once and is valid for a period of 48 hours only.</span><br/><br/>" +
                                    "<span>Please note that  registration (or sign up) for the app  is requested only to provide you with a <br/>seamless experience of using the app. Your registration information does not become part of <br/>the data collected for any study housed in the app. Each study has its own consent process <br/> and no data for any study will not be collected unless and until you provide an informed consent<br/> prior to joining the study </span><br/><br/>"+
                                    "<span>For any questions or assistance, please write to <a>"+configProp.get("support.email")+"</a> </span><br/><br/>" +
                                    "<span style='font-size:15px;'>Thanks,</span><br/><span>The FDA My Studies Platform Team</span>" +
                                    "<br/><span>----------------------------------------------------</span><br/>" +
                                    "<span style='font-size:10px;'>PS - This is an auto-generated email. Please do not reply.</span>" +
                                    "</div>" +
                                    "</body>" +
                                    "</html>";
                            FdahpUserRegUtil.sendMessage("Welcome to the FDA My Studies App!",message,addParticipantDetails.getEmail());
                            FdahpUserRegWSManager.addAuditEvent(addParticipantDetails.getUserId(),"User Registration Success","User Registration Success  with  email "+addParticipantDetails.getEmail()+".","FdaUserAuditEvent",getViewContext().getContainer().getId());
                            response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                        }else{
                            FdahpUserRegWSManager.addAuditEvent(null,"User Registration Failure","User Registration Failure  with  email "+addParticipantDetails.getEmail()+".","FdaUserAuditEvent",getViewContext().getContainer().getId());
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.FAILURE.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    if (StringUtils.trimToNull(participantForm.getEmailId()) == null)
                        errors.rejectValue("emailId",ERROR_MSG,"email is required.");
                    if (StringUtils.trimToNull(participantForm.getPassword()) == null)
                        errors.rejectValue("password",ERROR_MSG,"password is required.");
                }
            }catch (Exception e){
                 _log.error("register action:",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;

            }
            return response;
        }
    }
    public static class UserForm {

        public String _userId;
        public String _reason;

        public String getUserId()
        {
            return _userId;
        }

        public void setUserId(String userId)
        {
            _userId = userId;
        }

        public String getReason()
        {
            return _reason;
        }

        public void setReason(String reason)
        {
            _reason = reason;
        }
    }

    /**
     * Checking for email is verified or not
     *
     */
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class ConfirmRegistrationAction extends ApiAction{

        @Override
        public Object execute(Object o, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            try{
                String userId = getViewContext().getRequest().getHeader("userId");
                String auth = getViewContext().getRequest().getHeader("auth");
                boolean isAuthenticated = false;
                if(auth != null && StringUtils.isNotEmpty(auth))
                {
                    isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                    if (isAuthenticated)
                    {
                        if(userId != null && StringUtils.isNotEmpty(userId)){
                            UserDetails participantDetails =  FdahpUserRegWSManager.get().getParticipantDetails(userId);
                            if(participantDetails != null){
                                if(participantDetails.getStatus() == 2)
                                    response.put("verified", false);
                                if(participantDetails.getStatus() == 1)
                                    response.put("verified", true);
                                response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(), FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());

                            }else{
                                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.NO_DATA_AVAILABLE.getValue(),FdahpUserRegUtil.ErrorCodes.NO_DATA_AVAILABLE.getValue(), getViewContext().getResponse());
                                response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(), FdahpUserRegUtil.ErrorCodes.FAILURE.getValue().toLowerCase());
                            }
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }

            }catch (Exception e){
                _log.error("ConfirmRegistration action:",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }
    public static class VerificationForm {

        public String _emailId;
        public String _code;

        public String getEmailId()
        {
            return _emailId;
        }

        public void setEmailId(String emailId)
        {
            _emailId = emailId;
        }

        public String getCode()
        {
            return _code;
        }

        public void setCode(String code)
        {
            _code = code;
        }
    }

    /**
     * Verification  of an register email
     *
     */
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class VerifyAction extends ApiAction<VerificationForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public Object execute(VerificationForm verificationForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            try{
                if(verificationForm != null){
                    if((verificationForm.getEmailId() != null && StringUtils.isNotEmpty(verificationForm.getEmailId())) &&
                            (verificationForm.getCode() != null && StringUtils.isNotEmpty(verificationForm.getCode()))){
                        UserDetails participantDetails = participantDetails = FdahpUserRegWSManager.get().getParticipantDetailsByEmail(verificationForm.getEmailId());
                        if(null != participantDetails){
                            if(participantDetails.getSecurityToken() != null && participantDetails.getSecurityToken().equalsIgnoreCase(verificationForm.getCode())){
                                if(participantDetails.getStatus() == 2){
                                    String hours = (String) configProp.get("verification.expiration.in.hour");
                                    Date validateDate  = FdahpUserRegUtil.addHours(FdahpUserRegUtil.getCurrentDateTime(),Integer.parseInt(hours));
                                    if(participantDetails.getVerificationDate().before(validateDate) || participantDetails.getVerificationDate().equals(validateDate)){
                                        participantDetails.setStatus(1);
                                        participantDetails.setVerificationDate(FdahpUserRegUtil.getCurrentUtilDateTime());
                                        UserDetails updateParticipantDetails = FdahpUserRegWSManager.get().saveParticipant(participantDetails);
                                        if(null != updateParticipantDetails){
                                            response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(), FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                                            FdahpUserRegWSManager.addAuditEvent(participantDetails.getUserId(),"User Verification","User has confirmed registration through email "+verificationForm.getEmailId()+".","FdaUserAuditEvent",getViewContext().getContainer().getId());
                                        }else{
                                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                                            return null;
                                        }
                                    }else{
                                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_103.getValue(),FdahpUserRegUtil.ErrorCodes.CODE_EXPIRED.getValue(),FdahpUserRegUtil.ErrorCodes.CODE_EXPIRED.getValue(), getViewContext().getResponse());
                                        return null;
                                    }
                                }else{
                                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_103.getValue(),FdahpUserRegUtil.ErrorCodes.USER_ALREADY_VERIFIED.getValue(),FdahpUserRegUtil.ErrorCodes.USER_ALREADY_VERIFIED.getValue(), getViewContext().getResponse());
                                    return null;
                                }

                            }else{
                                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_CODE.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_CODE.getValue(), getViewContext().getResponse());
                                return null;
                            }
                        }else{
                             FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.EMAIL_NOT_EXISTS.getValue(),FdahpUserRegUtil.ErrorCodes.EMAIL_NOT_EXISTS.getValue(), getViewContext().getResponse());
                             return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }
            }catch (Exception e){
                _log.error("ConfirmRegistration action:",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    /**
     * login into application
     *
     */
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class LoginAction extends ApiAction<LoginForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public ApiResponse execute(LoginForm loginForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            UserDetails participantDetails = null;
            int maxAttemptsCount = Integer.valueOf((String)configProp.get("max.login.attempts"));
            try{
                if(loginForm != null){
                    if((loginForm.getEmailId() != null && StringUtils.isNotEmpty(loginForm.getEmailId())) && (loginForm.getPassword() != null && StringUtils.isNotEmpty(loginForm.getPassword()))){
                        participantDetails = FdahpUserRegWSManager.get().getParticipantDetailsByEmail(loginForm.getEmailId());
                        if(null != participantDetails){
                            LoginAttempts loginAttempts = FdahpUserRegWSManager.get().getLoginAttempts(loginForm.getEmailId());

                            if(loginAttempts != null && loginAttempts.getAttempts() == maxAttemptsCount){
                                int count = Integer.valueOf((String)configProp.get("expiration.login.attempts.minute"));
                                Date attemptsExpireDate = FdahpUserRegUtil.addMinutes(loginAttempts.getLastModified().toString(),count);
                                if(attemptsExpireDate.before(FdahpUserRegUtil.getCurrentUtilDateTime()) || attemptsExpireDate.equals(FdahpUserRegUtil.getCurrentUtilDateTime())){
                                    FdahpUserRegWSManager.get().resetLoginAttempts(loginForm.getEmailId());
                                    response = getLoginInformation(participantDetails,loginForm.getEmailId(),loginForm.getPassword(),maxAttemptsCount);
                                }else{
                                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.ACCOUNT_LOCKED.name(), FdahpUserRegUtil.ErrorCodes.ACCOUNT_LOCKED.getValue(), getViewContext().getResponse());
                                    return null;
                                }

                            }else{
                                response = getLoginInformation(participantDetails,loginForm.getEmailId(),loginForm.getPassword(),maxAttemptsCount);
                            }
                        }else{
                            FdahpUserRegWSManager.addAuditEvent(null,"FAILED SIGN IN","Wrong information entered in email "+loginForm.getEmailId()+". Which is not existed.","FdaUserAuditEvent",getViewContext().getContainer().getId());
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_CREDENTIALS.name(), FdahpUserRegUtil.ErrorCodes.INVALID_CREDENTIALS.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.name(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.name(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }


            }catch (Exception e){
                _log.error("Login Action:",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.name(), FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    /**
     * Get the Login information of registered user
     * @param participantDetails
     * @param email
     * @param password
     * @param maxAttemptsCount
     * @return
     */
    public ApiSimpleResponse getLoginInformation(UserDetails participantDetails,String email,String password,int maxAttemptsCount){
        ApiSimpleResponse response = new ApiSimpleResponse();
        if(participantDetails.getPassword() != null && participantDetails.getPassword().equalsIgnoreCase(FdahpUserRegUtil.getEncryptedString(password))){
            if(participantDetails.getTempPassword()){
                participantDetails.setResetPassword(null);
                participantDetails.setTempPassword(false);
                participantDetails.setTempPasswordDate(FdahpUserRegUtil.getCurrentUtilDateTime());
                FdahpUserRegWSManager.get().saveParticipant(participantDetails);
            }
            AuthInfo authInfo = FdahpUserRegWSManager.get().saveAuthInfo(participantDetails.getUserId(),true);
            if(authInfo != null){
                response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                response.put("userId",participantDetails.getUserId());
                response.put("auth",authInfo.getAuthKey());
                response.put("refreshToken",authInfo.getRefreshToken());
                if(participantDetails.getStatus() == 2)
                {
                    response.put("verified", false);
                }
                if(participantDetails.getStatus() == 1){
                    response.put("verified", true);
                }
                if(participantDetails.getPasswordUpdatedDate() != null){
                    String days = (String) configProp.get("password.expiration.in.day");
                    Date expiredDate = FdahpUserRegUtil.addDays(FdahpUserRegUtil.getCurrentDateTime(), Integer.parseInt(days));
                    _log.info("expiredDate:"+expiredDate+"participantDetails.getPasswordUpdatedDate():"+participantDetails.getPasswordUpdatedDate());
                    if(expiredDate.before(participantDetails.getPasswordUpdatedDate()) || expiredDate.equals(participantDetails.getPasswordUpdatedDate())){
                        response.put("resetPassword", true);
                    }
                }
                FdahpUserRegWSManager.get().resetLoginAttempts(email);
                FdahpUserRegWSManager.addAuditEvent(participantDetails.getUserId(),"SIGN IN","User Signed In.(User ID =  "+participantDetails.getUserId()+").","FdaUserAuditEvent",getViewContext().getContainer().getId());
            }  else{
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.name(), FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }

        }else if(participantDetails.getResetPassword() != null && participantDetails.getResetPassword().equalsIgnoreCase(FdahpUserRegUtil.getEncryptedString(password))){
            if(participantDetails.getTempPassword()){
                String hours = (String) configProp.get("verification.expiration.in.hour");
                Date validateDate  = FdahpUserRegUtil.addHours(FdahpUserRegUtil.getCurrentDateTime(),Integer.parseInt(hours));
                if(participantDetails.getTempPasswordDate().before(validateDate) || participantDetails.getTempPasswordDate().equals(validateDate)){
                    AuthInfo authInfo = FdahpUserRegWSManager.get().saveAuthInfo(participantDetails.getUserId(),true);
                    if(authInfo != null){
                        response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                        response.put("userId",participantDetails.getUserId());
                        response.put("auth",authInfo.getAuthKey());
                        response.put("refreshToken",authInfo.getRefreshToken());
                        if(participantDetails.getStatus() == 2)
                        {
                            response.put("verified", false);
                        }
                        if(participantDetails.getStatus() == 1){
                            response.put("verified", true);
                        }
                        response.put("resetPassword", participantDetails.getTempPassword());
                        FdahpUserRegWSManager.get().resetLoginAttempts(email);
                        FdahpUserRegWSManager.addAuditEvent(participantDetails.getUserId(),"SIGN IN","User Signed In.(User ID =  "+participantDetails.getUserId()+") with temp password.","FdaUserAuditEvent",getViewContext().getContainer().getId());
                    } else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.name(), FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_103.getValue(),FdahpUserRegUtil.ErrorCodes.CODE_EXPIRED.getValue(),FdahpUserRegUtil.ErrorCodes.CODE_EXPIRED.getValue(), getViewContext().getResponse());
                    return null;
                }
            }
        }else{
            FdahpUserRegWSManager.addAuditEvent(participantDetails.getUserId(),"FAILED SIGN IN","User Sign-In Failed. (User ID = "+participantDetails.getUserId()+")","FdaUserAuditEvent",getViewContext().getContainer().getId());
            LoginAttempts failAttempts = FdahpUserRegWSManager.get().updateLoginFailureAttempts(email);
            _log.info("maxAttemptsCount:"+maxAttemptsCount);
            if(failAttempts != null && failAttempts.getAttempts() == maxAttemptsCount){
                _log.info("failAttempts:"+failAttempts.getAttempts()+"maxAttemptsCount:"+maxAttemptsCount);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.ACCOUNT_LOCKED.name(), FdahpUserRegUtil.ErrorCodes.ACCOUNT_LOCKED.getValue(), getViewContext().getResponse());
                return null;
            }else{
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_USERNAME_PASSWORD_MSG.name(), FdahpUserRegUtil.ErrorCodes.INVALID_USERNAME_PASSWORD_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
        }
        return response;
    }

    /**
     * Get the UserDetails based on the userId
     * @param form
     * @return UserDetails
     */
    private UserDetails getParticipant(ParticipantForm form){
        UserDetails participantDetails = null;
        if(null != form.getUserId()){
            participantDetails = FdahpUserRegWSManager.get().getParticipantDetails(form.getUserId());
        }
        if (participantDetails == null){
            participantDetails = new UserDetails();
            participantDetails.setStatus(2);
            String token = RandomStringUtils.randomAlphanumeric(6);
            participantDetails.setSecurityToken(token);
            participantDetails.setVerificationDate(FdahpUserRegUtil.getCurrentUtilDateTime());
            participantDetails.setPasswordUpdatedDate(FdahpUserRegUtil.getCurrentUtilDateTime());
            String userId = UUID.randomUUID().toString();
            participantDetails.setUserId(userId);
        }
        if(form.getFirstName() != null)
            participantDetails.setFirstName(form.getFirstName());
        if(form.getLastName() != null)
            participantDetails.setLastName(form.getLastName());
        if(form.getEmailId() !=null)
            participantDetails.setEmail(form.getEmailId());
        if(form.getPassword() != null)
            participantDetails.setPassword(FdahpUserRegUtil.getEncryptedString(form.getPassword()));
        if(form.getUsePassCode() != null)
            participantDetails.setUsePassCode(form.getUsePassCode());
        if(form.getLocalNotification() != null)
            participantDetails.setLocalNotificationFlag(form.getLocalNotification());
        if(form.getRemoteNotification() != null)
            participantDetails.setRemoteNotificationFlag(form.getRemoteNotification());
        if(form.getTouchId() != null)
            participantDetails.setTouchId(form.getTouchId());
        return participantDetails;
    }

    /**
     * Forgot password of an register email
     */
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class ForgotPasswordAction extends ApiAction<LoginForm>
    {

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public ApiResponse execute(LoginForm loginForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            int maxAttemptsCount = Integer.valueOf((String)configProp.get("max.login.attempts"));
            try{
                if(loginForm != null && loginForm.getEmailId() != null && StringUtils.isNotEmpty(loginForm.getEmailId())){
                    UserDetails participantDetails = FdahpUserRegWSManager.get().getParticipantDetailsByEmail(loginForm.getEmailId());
                    if(participantDetails != null){
                        if(participantDetails.getStatus() == 1){
                            boolean isValid= true;
                            LoginAttempts loginAttempts = FdahpUserRegWSManager.get().getLoginAttempts(loginForm.getEmailId());
                            if(loginAttempts != null && loginAttempts.getAttempts() == maxAttemptsCount)
                            {
                                int count = Integer.valueOf((String)configProp.get("expiration.login.attempts.minute"));
                                Date attemptsExpireDate = FdahpUserRegUtil.addMinutes(loginAttempts.getLastModified().toString(), count);
                                if (attemptsExpireDate.before(FdahpUserRegUtil.getCurrentUtilDateTime()) || attemptsExpireDate.equals(FdahpUserRegUtil.getCurrentUtilDateTime()))
                                {
                                    isValid= true;
                                }else{
                                    isValid= false;
                                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.ACCOUNT_TEMP_LOCKED.name(), FdahpUserRegUtil.ErrorCodes.ACCOUNT_TEMP_LOCKED.getValue(), getViewContext().getResponse());
                                    return null;
                                }
                            }else{
                                isValid= true;
                            }
                            UserDetails upParticipantDetails = null;
                            String tempPassword = RandomStringUtils.randomAlphanumeric(6);
                            if(isValid){
                                participantDetails.setTempPassword(true);
                                participantDetails.setResetPassword(FdahpUserRegUtil.getEncryptedString(tempPassword));
                                participantDetails.setTempPasswordDate(FdahpUserRegUtil.getCurrentUtilDateTime());
                                upParticipantDetails = FdahpUserRegWSManager.get().saveParticipant(participantDetails);
                            }
                            if(upParticipantDetails != null){
                                String message="<html>" +
                                        "<body>" +
                                        "<div style='margin:20px;padding:10px;font-family: sans-serif;font-size: 14px;'>" +
                                        "<span>Hi,</span><br/><br/>" +
                                        "<span>Thank you for reaching out for password help.</span><br/><br/>" +
                                        "<span>Here is a temporary password which you can use to sign in to the FDA My Studies App.<br/> You will be required to set up a new password after signing in.</span><br/><br/>" +
                                        "<span><strong>Temporary Password:</strong> " + tempPassword + "</span><br/><br/>" +
                                        "<span>Please note that this temporary password can be used only once and is valid for a period of 48 hours only.</span><br/><br/>" +
                                        "<span>For any questions or assistance, please write to <a>"+configProp.get("support.email")+"</a> </span><br/><br/>" +
                                        "<span style='font-size:15px;'>Thanks,</span><br/><span>The FDA My Studies Platform Team</span>" +
                                        "<br/><span>----------------------------------------------------</span><br/>" +
                                        "<span style='font-size:10px;'>PS - This is an auto-generated email. Please do not reply. In case you did not request password help, please visit the app and change your password as a precautionary measure.</span>" +
                                        "</div>" +
                                        "</body>" +
                                        "</html>";
                                FdahpUserRegUtil.sendMessage("Password Help - FDA My Studies App!",message,participantDetails.getEmail());
                                response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                                FdahpUserRegWSManager.get().resetLoginAttempts(loginForm.getEmailId());
                                FdahpUserRegWSManager.addAuditEvent(participantDetails.getUserId(),"PASSWORD HELP","Password Help sent to user.(User ID = "+participantDetails.getUserId()+")","FdaUserAuditEvent",getViewContext().getContainer().getId());
                            }else{
                                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.EMAIL_NOT_VERIFIED.getValue(), FdahpUserRegUtil.ErrorCodes.EMAIL_NOT_VERIFIED.getValue(), getViewContext().getResponse());
                                return null;
                            }
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_103.getValue(),FdahpUserRegUtil.ErrorCodes.EMAIL_NOT_VERIFIED.getValue(), FdahpUserRegUtil.ErrorCodes.EMAIL_NOT_VERIFIED.getValue(), getViewContext().getResponse());
                            return null;
                        }


                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_CREDENTIALS.getValue(), FdahpUserRegUtil.ErrorCodes.INVALID_CREDENTIALS.getValue(), getViewContext().getResponse());
                        errors.rejectValue("emailId",ERROR_MSG,FdahpUserRegUtil.ErrorCodes.INVALID_CREDENTIALS.getValue());
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }
            }catch (Exception e){
                 _log.error("ForgotPassword Action Error:",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    /**
     * Resending the confirmation email
     */
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class ResendConfirmationAction extends  ApiAction<LoginForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public ApiResponse execute(LoginForm loginForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            String auth = getViewContext().getRequest().getHeader("auth");
            boolean isAuthenticated = false;
            String code ="";
            try
            {
                if(loginForm != null && loginForm.getEmailId() != null && StringUtils.isNotEmpty(loginForm.getEmailId())){
                    UserDetails participantDetails = FdahpUserRegWSManager.get().getParticipantDetailsByEmail(loginForm.getEmailId());
                    if(participantDetails != null ){
                        if(participantDetails.getStatus() == 2){
                            code = RandomStringUtils.randomAlphanumeric(6);
                            participantDetails.setSecurityToken(code);
                            participantDetails.setVerificationDate(FdahpUserRegUtil.getCurrentUtilDateTime());
                            FdahpUserRegWSManager.get().saveParticipant(participantDetails);
                            String message = "<html>" +
                                    "<body>" +
                                    "<div style='margin:20px;padding:10px;font-family: sans-serif;font-size: 14px;'>" +
                                    "<span>Hi,</span><br/><br/>" +
                                    "<span>Thank you for registering with us! We look forward to having you on board and actively taking part in<br/>research studies conducted by the FDA and its partners.</span><br/><br/>" +
                                    "<span>Your sign-up process is almost complete. Please use the verification code provided below to<br/>complete the Verification step in the mobile app.</span><br/><br/>" +
                                    "<span><strong>Verification Code:</strong>" +participantDetails.getSecurityToken()+ "</span><br/><br/>" +
                                    "<span>This code can be used only once and is valid for a period of 48 hours only.</span><br/><br/>" +
                                    "<span>Please note that  registration (or sign up) for the app  is requested only to provide you with a <br/>seamless experience of using the app. Your registration information does not become part of <br/>the data collected for any study housed in the app.Each study has its own consent process, <br/>and no data for any study will be collected unless and until you provide an informed consent<br/> prior to joining the study. </span><br/><br/>"+
                                    "<span>For any questions or assistance, please write to <a>"+configProp.get("support.email")+"</a> </span><br/><br/>" +
                                    "<span style='font-size:15px;'>Thanks,</span><br/><span>The FDA My Studies Platform Team</span>" +
                                    "<br/><span>----------------------------------------------------</span><br/>" +
                                    "<span style='font-size:10px;'>PS - This is an auto-generated email. Please do not reply.</span>" +
                                    "</div>" +
                                    "</body>" +
                                    "</html>";
                           FdahpUserRegUtil.sendMessage("Welcome to the FDA My Studies App!",message,participantDetails.getEmail());
                           response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                           FdahpUserRegWSManager.addAuditEvent(participantDetails.getUserId(),"Requested Confirmation mail","Confirmation mail has been sent again to"+participantDetails.getEmail()+".","FdaUserAuditEvent",getViewContext().getContainer().getId());
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_103.getValue(),FdahpUserRegUtil.ErrorCodes.USER_ALREADY_VERIFIED.getValue(), FdahpUserRegUtil.ErrorCodes.USER_ALREADY_VERIFIED.getValue(), getViewContext().getResponse());
                            return null;
                        }

                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.EMAIL_NOT_EXISTS.getValue(), FdahpUserRegUtil.ErrorCodes.EMAIL_NOT_EXISTS.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }

            }catch (Exception e){
                _log.error("ResendConfirmationAction Action Error:",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    /**
     * Updating the user password
     */
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class ChangePasswordAction extends ApiAction<ChangePasswordForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public ApiResponse execute(ChangePasswordForm form, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            String auth = getViewContext().getRequest().getHeader("auth");
            boolean isAuthenticated = false;
            List<PasswordHistory> passwordHistories = null;
            Boolean isValidPassword = true;
            try{
                if(auth != null && StringUtils.isNotEmpty(auth)){
                    isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                    if(isAuthenticated){
                        String oldPassword = form.getCurrentPassword();
                        String newPassword = form.getNewPassword();
                        String userId = getViewContext().getRequest().getHeader("userId");
                        if((oldPassword != null && StringUtils.isNotEmpty(oldPassword)) && (newPassword != null && StringUtils.isNotEmpty(newPassword))){
                            UserDetails participantDetails = FdahpUserRegWSManager.get().getParticipantDetails(userId);
                            if(participantDetails != null ){
                                if((participantDetails.getPassword() != null && participantDetails.getPassword().equalsIgnoreCase(FdahpUserRegUtil.getEncryptedString(oldPassword))) || (participantDetails.getResetPassword() != null && participantDetails.getResetPassword().equalsIgnoreCase(FdahpUserRegUtil.getEncryptedString(oldPassword)))){
                                    if(!oldPassword.equals(newPassword)){
                                        passwordHistories = FdahpUserRegWSManager.get().getPasswordHistoryList(userId);
                                        if(passwordHistories != null && !passwordHistories.isEmpty()){
                                            for (PasswordHistory userPasswordHistory : passwordHistories) {
                                                if(FdahpUserRegUtil.getEncryptedString(newPassword).equalsIgnoreCase(userPasswordHistory.getPassword())){
                                                    isValidPassword = false;
                                                    break;
                                                }
                                            }
                                        }
                                        if(isValidPassword){
                                            participantDetails.setPassword(FdahpUserRegUtil.getEncryptedString(newPassword));
                                            if(participantDetails.getTempPassword())
                                                participantDetails.setTempPassword(false);
                                            participantDetails.setResetPassword(null);
                                            participantDetails.setTempPasswordDate(FdahpUserRegUtil.getCurrentUtilDateTime());
                                            participantDetails.setPasswordUpdatedDate(FdahpUserRegUtil.getCurrentUtilDateTime());
                                            UserDetails updParticipantDetails = FdahpUserRegWSManager.get().saveParticipant(participantDetails);
                                            if(updParticipantDetails != null && !participantDetails.getTempPassword()){
                                                String message = FdahpUserRegWSManager.get().savePasswordHistory(userId,FdahpUserRegUtil.getEncryptedString(newPassword));
                                                if(message.equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue()))
                                                    response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                                                    FdahpUserRegWSManager.addAuditEvent(participantDetails.getUserId(),"Change Password","User password changed successfully "+participantDetails.getEmail()+".","FdaUserAuditEvent",getViewContext().getContainer().getId());

                                            }

                                        }else{
                                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.NEW_PASSWORD_NOT_SAME_LAST_PASSWORD.getValue(),FdahpUserRegUtil.ErrorCodes.NEW_PASSWORD_NOT_SAME_LAST_PASSWORD.getValue(), getViewContext().getResponse());
                                            errors.rejectValue("currentPassword",ERROR_MSG,FdahpUserRegUtil.ErrorCodes.NEW_PASSWORD_NOT_SAME_LAST_PASSWORD.getValue());
                                        }

                                    }else{
                                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.OLD_PASSWORD_AND_NEW_PASSWORD_NOT_SAME.getValue(), getViewContext().getResponse());
                                        errors.rejectValue("currentPassword",ERROR_MSG,FdahpUserRegUtil.ErrorCodes.OLD_PASSWORD_AND_NEW_PASSWORD_NOT_SAME.getValue());
                                    }

                                }else{
                                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.OLD_PASSWORD_NOT_EXISTS.getValue(), getViewContext().getResponse());
                                    errors.rejectValue("currentPassword",ERROR_MSG,FdahpUserRegUtil.ErrorCodes.OLD_PASSWORD_NOT_EXISTS.getValue());
                                }
                            } else{
                                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                                return null;
                            }

                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }

                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }
            }catch (Exception e){
                 _log.error("ChangePassword Action Error",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }
    public static class ChangePasswordForm extends ReturnUrlForm{
        private String _currentPassword;
        private String _newPassword;
        private  String _userId;

        public String getCurrentPassword()
        {
            return _currentPassword;
        }

        public void setCurrentPassword(String currentPassword)
        {
            _currentPassword = currentPassword;
        }

        public String getNewPassword()
        {
            return _newPassword;
        }

        public void setNewPassword(String newPassword)
        {
            _newPassword = newPassword;
        }

        public String getUserId()
        {
            return _userId;
        }

        public void setUserId(String userId)
        {
            _userId = userId;
        }
    }

    /**
     * Logout from app
     */
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class LogoutAction extends  ApiAction<UserForm>{

        @Override
        public Object execute(UserForm userForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            boolean isAuthenticated = false;
            String message = FdahpUserRegUtil.ErrorCodes.FAILURE.getValue();
            try
            {
                 if(isDelete()){
                    String auth = getViewContext().getRequest().getHeader("auth");
                    String userId = getViewContext().getRequest().getHeader("userId");

                    if(auth != null && StringUtils.isNotEmpty(auth)){
                        isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                        if(isAuthenticated){
                            if(null != userId && StringUtils.isNotEmpty(userId)){
                                message = FdahpUserRegWSManager.get().signout(userId);
                                if(message.equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue())){
                                    response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                                    FdahpUserRegWSManager.addAuditEvent(userId,"SIGN OUT","User Signed Out. (User ID = "+userId+") ","FdaUserAuditEvent",getViewContext().getContainer().getId());
                                }else{
                                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(), FdahpUserRegUtil.ErrorCodes.FAILURE.getValue(), getViewContext().getResponse());
                                    return null;
                                }
                            }else{
                                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                                return null;
                            }
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }

                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the DELETE method when calling this action.");
                    return null;
                }

            }catch (Exception e){
                 _log.error("Logout Action Error:",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    /**
     * Get the User Profile information
     */
    @Marshal(Marshaller.Jackson)
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class UserProfileAction extends ApiAction<Object>{

        @Override
        public ApiResponse execute(Object object, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            boolean isAuthenticated = false;
            try{
                String auth = getViewContext().getRequest().getHeader("auth");
                String userId = getViewContext().getRequest().getHeader("userId");
                if(auth != null && StringUtils.isNotEmpty(auth)){
                    isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                    if(isAuthenticated){
                        if(null != userId && StringUtils.isNotEmpty(userId)){
                            response = FdahpUserRegWSManager.get().getParticipantInfoDetails(userId);
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }
            }catch (Exception e){
                _log.error("User Profile Action",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    /**
     * Update the user profile section
     */
    @Marshal(Marshaller.Jackson)
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class UpdateUserProfileAction extends  ApiAction<ProfileForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }
        @Override
        public ApiResponse execute(ProfileForm profileForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            List<ParticipantStudies> addParticipantStudiesList = new ArrayList<ParticipantStudies>();
            String message = FdahpUserRegUtil.ErrorCodes.FAILURE.getValue();
            AuthInfo updaAuthInfo = null;
            try{
                String auth = getViewContext().getRequest().getHeader("auth");
                String userId = getViewContext().getRequest().getHeader("userId");
                boolean isAuthenticated = false;
                if(auth != null && StringUtils.isNotEmpty(auth))
                {
                    isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                    if (isAuthenticated)
                    {
                        if (profileForm != null && userId != null && StringUtils.isNotEmpty(userId))
                        {
                            UserDetails participantDetails = FdahpUserRegWSManager.get().getParticipantDetails(userId);
                            if (participantDetails != null)
                            {
                                if (profileForm.getSettings() != null)
                                {
                                    if (profileForm.getSettings().getRemoteNotifications() != null){
                                        participantDetails.setRemoteNotificationFlag(profileForm.getSettings().getRemoteNotifications());
                                        AuthInfo authInfo = FdahpUserRegWSManager.get().getAuthInfo(auth, userId);
                                        if(authInfo != null){
                                            authInfo.setRemoteNotificationFlag(profileForm.getSettings().getRemoteNotifications());
                                            FdahpUserRegWSManager.get().updateAuthInfo(authInfo);
                                        }
                                    }
                                    if (profileForm.getSettings().getLocalNotifications() != null)
                                        participantDetails.setLocalNotificationFlag(profileForm.getSettings().getLocalNotifications());
                                    if (profileForm.getSettings().getPasscode() != null)
                                        participantDetails.setUsePassCode(profileForm.getSettings().getPasscode());
                                    if (profileForm.getSettings().getTouchId() != null)
                                        participantDetails.setTouchId(profileForm.getSettings().getTouchId());
                                    if(profileForm.getSettings().getReminderLeadTime() != null && StringUtils.isNotEmpty(profileForm.getSettings().getReminderLeadTime()))
                                        participantDetails.setReminderLeadTime(profileForm.getSettings().getReminderLeadTime());

                                    if(profileForm.getSettings().getLocale() != null && StringUtils.isNotEmpty(profileForm.getSettings().getLocale()))
                                        participantDetails.setLocale(profileForm.getSettings().getLocale());
                                }
                                if (profileForm.getInfo() != null)
                                {
                                    AuthInfo authInfo = FdahpUserRegWSManager.get().getAuthInfo(auth, userId);
                                    if (authInfo != null)
                                    {
                                        if (profileForm.getInfo().getOs() != null && StringUtils.isNotEmpty(profileForm.getInfo().getOs()))
                                        {
                                            authInfo.setDeviceType(profileForm.getInfo().getOs());
                                        }
                                        if (profileForm.getInfo().getOs() != null && StringUtils.isNotEmpty(profileForm.getInfo().getOs()) && (profileForm.getInfo().getOs().equalsIgnoreCase("IOS") || profileForm.getInfo().getOs().equalsIgnoreCase("I")))
                                        {
                                            authInfo.setIosAppVersion(profileForm.getInfo().getAppVersion());
                                        }
                                        else
                                        {
                                            authInfo.setAndroidAppVersion(profileForm.getInfo().getAppVersion());
                                        }
                                        if (profileForm.getInfo().getDeviceToken() != null && StringUtils.isNotEmpty(profileForm.getInfo().getDeviceToken()))
                                        {
                                            authInfo.setDeviceToken(profileForm.getInfo().getDeviceToken());
                                        }
                                        updaAuthInfo = FdahpUserRegWSManager.get().updateAuthInfo(authInfo);
                                    }
                                }
                                UserDetails updateParticipantDetails = FdahpUserRegWSManager.get().saveParticipant(participantDetails);
                                if (updateParticipantDetails != null || message.equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue()) || updaAuthInfo != null)
                                {
                                    response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(), FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                                    FdahpUserRegWSManager.addAuditEvent(participantDetails.getUserId(),"PROFILE UPDATE","User Profile/Preferences updated.  (User ID = "+participantDetails.getUserId()+")","FdaUserAuditEvent",getViewContext().getContainer().getId());
                                }
                            }else{
                                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                                return null;
                            }
                        }
                        else
                        {
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(), FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else
                    {
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
            }catch (Exception e){
                 _log.error("UpdateUSerProfile Action Error :",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    public static class ProfileForm {


        public ProfileBean _profile;
        public SettingsBean _settings;
        public InfoBean _info;
        public List<ParticipantInfoBean> _participantInfo;

        public ProfileBean getProfile()
        {
            return _profile;
        }

        public void setProfile(ProfileBean profile)
        {
            _profile = profile;
        }

        public SettingsBean getSettings()
        {
            return _settings;
        }

        public void setSettings(SettingsBean settings)
        {
            _settings = settings;
        }

        public InfoBean getInfo()
        {
            return _info;
        }

        public void setInfo(InfoBean info)
        {
            _info = info;
        }

        public List<ParticipantInfoBean> getParticipantInfo()
        {
            return _participantInfo;
        }

        public void setParticipantInfo(List<ParticipantInfoBean> participantInfo)
        {
            _participantInfo = participantInfo;
        }


    }

    /**
     * Update the user preferences
     */
    @Marshal(Marshaller.Jackson)
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class UpdatePreferencesAction extends  ApiAction<PreferencesForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public ApiResponse execute(PreferencesForm preferencesForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response  = new ApiSimpleResponse();
            List<ParticipantStudies> addParticipantStudiesList = new ArrayList<ParticipantStudies>();
            List<ParticipantActivities> participantActivitiesList = new ArrayList<ParticipantActivities>();
            try{
                String auth = getViewContext().getRequest().getHeader("auth");
                String userId = getViewContext().getRequest().getHeader("userId");
                boolean isAuthenticated = false;
                if(auth != null && StringUtils.isNotEmpty(auth)){
                    isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                    if(isAuthenticated){
                        if(preferencesForm != null && userId != null && StringUtils.isNotEmpty(userId)){
                            if(preferencesForm.getStudies() != null && preferencesForm.getStudies().size() > 0){
                               List<StudiesBean> studiesBeenList = preferencesForm.getStudies();
                                List<ParticipantStudies> existParticipantStudies = FdahpUserRegWSManager.get().getParticipantStudiesList(userId);

                                for (int i=0;i < studiesBeenList.size() ; i++){
                                    StudiesBean studiesBean =  studiesBeenList.get(i);
                                    boolean isExists = false;
                                    if(existParticipantStudies != null && existParticipantStudies.size() >0){
                                         for (ParticipantStudies participantStudies : existParticipantStudies){

                                             if(studiesBean.getStudyId().equalsIgnoreCase(participantStudies.getStudyId())){
                                                 isExists = true;
                                                 if(studiesBean.getStatus() != null && StringUtils.isNotEmpty(studiesBean.getStatus()))
                                                     participantStudies.setStatus(studiesBean.getStatus());
                                                 if(studiesBean.getBookmarked() != null)
                                                     participantStudies.setBookmark(studiesBean.getBookmarked());
                                                 addParticipantStudiesList.add(participantStudies);
                                             }
                                         }
                                    }
                                    if(!isExists){
                                        ParticipantStudies participantStudies = new ParticipantStudies();
                                        if(studiesBean.getStudyId()!= null && StringUtils.isNotEmpty(studiesBean.getStudyId()))
                                            participantStudies.setStudyId(studiesBean.getStudyId());
                                        if(studiesBean.getStatus()!= null && StringUtils.isNotEmpty(studiesBean.getStatus())){
                                            participantStudies.setStatus(studiesBean.getStatus());
                                        }else{
                                            participantStudies.setStatus(FdahpUserRegUtil.ErrorCodes.YET_TO_JOIN.getValue());
                                        }
                                        if(studiesBean.getBookmarked() != null)
                                            participantStudies.setBookmark(studiesBean.getBookmarked());
                                        if(userId != null && StringUtils.isNotEmpty(userId))
                                            participantStudies.setUserId(userId);
                                       addParticipantStudiesList.add(participantStudies);
                                    }
                                }
                               FdahpUserRegWSManager.get().saveParticipantStudies(addParticipantStudiesList);
                            }
                            response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }
            }catch (Exception e){
                 _log.error("UpdatePreferences Action Error :",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }
    public static class PreferencesForm {


        public List<StudiesBean>  _studies;
        public List<ActivitiesBean>  _activity;
        public String _studyId;

        public List<StudiesBean> getStudies()
        {
            return _studies;
        }

        public void setStudies(List<StudiesBean> studies)
        {
            _studies = studies;
        }

        public List<ActivitiesBean> getActivity()
        {
            return _activity;
        }

        public void setActivity(List<ActivitiesBean> activity)
        {
            _activity = activity;
        }

        public String getStudyId()
        {
            return _studyId;
        }

        public void setStudyId(String studyId)
        {
            _studyId = studyId;
        }
    }



    public static class LoginForm{

        public  String _emailId;
        public  String  _password;

        public String getEmailId()
        {
            return _emailId;
        }

        public void setEmailId(String emailId)
        {
            _emailId = emailId;
        }

        public String getPassword()
        {
            return _password;
        }

        public void setPassword(String password)
        {
            _password = password;
        }
    }

    /**
     * Get the user preferences
     */
    @Marshal(Marshaller.Jackson)
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class UserPreferencesAction extends  ApiAction<UserForm>{

        @Override
        public ApiResponse execute(UserForm userForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            boolean isAuthenticated = false;
            try{
                String auth = getViewContext().getRequest().getHeader("auth");
                String userId =  getViewContext().getRequest().getHeader("userId");
                if(auth != null && StringUtils.isNotEmpty(auth)){
                    isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                    if(isAuthenticated){
                        if(userId != null && StringUtils.isNotEmpty(userId)){
                            response = FdahpUserRegWSManager.get().getPreferences(userId);
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }
            }catch (Exception e){
                 _log.error("UserPreferencesAction Action Error",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;

            }
            return response;
        }
    }


    public static class ConsentStatusForm{

        private  String _studyId;
        private  Boolean _eligibility;
        private ConsentBean _consent;
        private String _sharing;

        public String getStudyId()
        {
            return _studyId;
        }

        public void setStudyId(String studyId)
        {
            _studyId = studyId;
        }

        public Boolean getEligibility()
        {
            return _eligibility;
        }

        public void setEligibility(Boolean eligibility)
        {
            _eligibility = eligibility;
        }

        public ConsentBean getConsent()
        {
            return _consent;
        }

        public void setConsent(ConsentBean consent)
        {
            _consent = consent;
        }

        public String getSharing()
        {
            return _sharing;
        }

        public void setSharing(String sharing)
        {
            _sharing = sharing;
        }
    }

    /**
     * Updating the user eligibility consent status
     */
    @Marshal(Marshaller.Jackson)
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
   public class UpdateEligibilityConsentStatusAction extends  ApiAction<ConsentStatusForm>{

       @Override
       protected ModelAndView handleGet() throws Exception
       {
           getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
           return null;
       }

       @Override
       public ApiResponse execute(ConsentStatusForm consentStatusForm, BindException errors) throws Exception
       {
           ApiSimpleResponse response = new ApiSimpleResponse();
           boolean isAuthenticated = false;
           StudyConsent updateConsent = null;
           try{
               String auth = getViewContext().getRequest().getHeader("auth");
               String userId =  getViewContext().getRequest().getHeader("userId");
               if(auth != null && StringUtils.isNotEmpty(auth)){
                   isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                   if(isAuthenticated){
                       if(consentStatusForm != null && consentStatusForm.getConsent() != null){
                            if(consentStatusForm.getStudyId() != null && StringUtils.isNotEmpty(consentStatusForm.getStudyId()) && userId != null && StringUtils.isNotEmpty(userId)){
                                ParticipantStudies participantStudies = FdahpUserRegWSManager.get().getParticipantStudies(consentStatusForm.getStudyId(),userId);
                                if(participantStudies != null){
                                    if(consentStatusForm.getEligibility() != null){
                                        participantStudies.setEligbibility(consentStatusForm.getEligibility());
                                    }
                                    if(consentStatusForm.getSharing() != null && StringUtils.isNotEmpty(consentStatusForm.getSharing())){
                                        participantStudies.setSharing(consentStatusForm.getSharing());
                                    }
                                    List<ParticipantStudies> participantStudiesList = new ArrayList<ParticipantStudies>();
                                    participantStudiesList.add(participantStudies);
                                    String message = FdahpUserRegWSManager.get().saveParticipantStudies(participantStudiesList);
                                    if(consentStatusForm.getConsent() != null){
                                        StudyConsent consent = null;
                                        if(consentStatusForm.getConsent().getVersion() != null && StringUtils.isNotEmpty(consentStatusForm.getConsent().getVersion())){
                                            consent = FdahpUserRegWSManager.get().getStudyConsent(userId,consentStatusForm.getStudyId(),consentStatusForm.getConsent().getVersion());
                                            if(consent != null){
                                                if(consentStatusForm.getConsent().getVersion() != null && StringUtils.isNotEmpty(consentStatusForm.getConsent().getVersion()))
                                                    consent.setVersion(consentStatusForm.getConsent().getVersion());
                                                if(consentStatusForm.getConsent().getStatus() != null && StringUtils.isNotEmpty(consentStatusForm.getConsent().getStatus()))
                                                    consent.setStatus(consentStatusForm.getConsent().getStatus());
                                                if(consentStatusForm.getConsent().getPdf() != null && StringUtils.isNotEmpty(consentStatusForm.getConsent().getPdf())){
                                                    consent.setPdf(consentStatusForm.getConsent().getPdf());
                                                    String pdfPath = saveStudyConsentDocument(consent);
                                                    consent.setPdfPath(pdfPath);
                                                }
                                                consent.setUserId(userId);
                                                consent.setStudyId(consentStatusForm.getStudyId());
                                            }else{
                                                consent = new StudyConsent();
                                                consent.setUserId(userId);
                                                consent.setStudyId(consentStatusForm.getStudyId());
                                                consent.setStatus(consentStatusForm.getConsent().getStatus());
                                                consent.setVersion(consentStatusForm.getConsent().getVersion());
                                                consent.setPdf(consentStatusForm.getConsent().getPdf());
                                                if(consentStatusForm.getConsent().getPdf() != null && StringUtils.isNotEmpty(consentStatusForm.getConsent().getPdf())){
                                                    String pdfPath = saveStudyConsentDocument(consent);
                                                    consent.setPdfPath(pdfPath);
                                                }
                                            }
                                            updateConsent = FdahpUserRegWSManager.get().saveStudyConsent(consent);
                                            if(updateConsent != null && message.equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue())){
                                                response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),message);
                                                FdahpUserRegWSManager.addAuditEvent(userId,"Update eligibility consent status","Eligibility consent has been updated for study "+consentStatusForm.getStudyId()+".","FdaStudyAuditEvent",getViewContext().getContainer().getId());
                                            }else{
                                                response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.FAILURE.getValue());
                                            }
                                        }else{
                                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.CONSENT_VERSION_REQUIRED.getValue(),FdahpUserRegUtil.ErrorCodes.CONSENT_VERSION_REQUIRED.getValue(), getViewContext().getResponse());
                                            return null;
                                        }

                                    }

                                }else{
                                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.NO_DATA_AVAILABLE.getValue(),FdahpUserRegUtil.ErrorCodes.NO_DATA_AVAILABLE.getValue(), getViewContext().getResponse());
                                    return null;
                                }
                            }else{
                                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                                return null;
                           }
                       }else{
                           FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                           return null;
                       }
                   }else{
                       FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                       return null;
                   }
               }else{
                   FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                   return null;
               }
           }catch (Exception e){
                _log.error("UpdateEligibilityConsentStatusAction Action Error",e);
               FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
               return null;
           }
           return response;
       }
   }
   public static class ActivityForm {

       public String _userId;
       public String _studyId;
       public String _consentVersion;
       public ActivityStateForm _activity;

       public String getUserId()
       {
           return _userId;
       }

       public void setUserId(String userId)
       {
           _userId = userId;
       }

       public String getStudyId()
       {
           return _studyId;
       }

       public void setStudyId(String studyId)
       {
           _studyId = studyId;
       }

       public String getConsentVersion()
       {
           return _consentVersion;
       }

       public void setConsentVersion(String consentVersion)
       {
           _consentVersion = consentVersion;
       }

       public ActivityStateForm getActivity()
       {
           return _activity;
       }

       public void setActivity(ActivityStateForm activity)
       {
           _activity = activity;
       }
   }

    /**
     * Get activity state of an study to the user
     */
   @RequiresNoPermission
   @CSRF(CSRF.Method.NONE)
    public class ActivityStateAction extends ApiAction<ActivityForm>
   {

       @Override
       public ApiResponse execute(ActivityForm activityForm, BindException errors) throws Exception
       {
           ApiSimpleResponse response  = new ApiSimpleResponse();
           boolean isAuthenticated = false;
           try{
               String userId = getViewContext().getRequest().getHeader("userId");
               String auth = getViewContext().getRequest().getHeader("auth");
               if(auth != null && StringUtils.isNotEmpty(auth)){
                   isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                   if(isAuthenticated){
                       String studyId = getViewContext().getRequest().getParameter("studyId");
                       if(studyId != null && StringUtils.isNotEmpty(studyId) && userId != null && StringUtils.isNotEmpty(userId)){
                           List<ParticipantActivities> participantActivitiesList = FdahpUserRegWSManager.get().getParticipantActivitiesList(studyId,userId);
                           JSONArray jsonArray = new JSONArray();
                           if(participantActivitiesList !=null && participantActivitiesList.size() >0)
                           {
                               for (ParticipantActivities participantActivities : participantActivitiesList){
                                   JSONObject jsonObject = new JSONObject();
                                   if (participantActivities.getActivityId() != null && StringUtils.isNotEmpty(participantActivities.getActivityId()))
                                       jsonObject.put("activityId",participantActivities.getActivityId());
                                   if(participantActivities.getActivityVersion() != null && StringUtils.isNotEmpty(participantActivities.getActivityVersion()))
                                       jsonObject.put("activityVersion",participantActivities.getActivityVersion());
                                   if(participantActivities.getActivityState() != null && StringUtils.isNotEmpty(participantActivities.getActivityState()))
                                       jsonObject.put("activityState",participantActivities.getActivityState());
                                   if(participantActivities.getActivityRunId() != null && StringUtils.isNotEmpty(participantActivities.getActivityRunId()))
                                       jsonObject.put("activityRunId",participantActivities.getActivityRunId());
                                   if(participantActivities.getBookmark() != null )
                                       jsonObject.put("bookmarked",participantActivities.getBookmark());
                                   JSONObject runObject = new JSONObject();
                                   if(participantActivities.getTotal() != null)
                                       runObject.put("total",participantActivities.getTotal());
                                   if(participantActivities.getCompleted() != null)
                                       runObject.put("completed",participantActivities.getCompleted());
                                   if(participantActivities.getMissed() != null)
                                       runObject.put("missed",participantActivities.getMissed());
                                   jsonObject.put("activityRun",runObject);
                                   jsonArray.put(jsonObject);
                               }
                           }
                           response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                           response.put(FdahpUserRegUtil.ErrorCodes.ACTIVITIES.getValue(),jsonArray);
                       }else{
                           FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                           return null;
                       }
                   }else{
                       FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                       return null;
                   }
               }else{
                   FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                   return null;
               }
           }catch (Exception e){
                _log.error("ActivityStateAction Action Error",e);
               FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
               return null;

           }
           return response;
       }
   }

   public static class ActivityStateForm {


       private String _activityId;
       private String _activityVersion;
       private String _activityState;
       private String _activityRunId;


       public String getActivityId()
       {
           return _activityId;
       }

       public void setActivityId(String activityId)
       {
           _activityId = activityId;
       }

       public String getActivityVersion()
       {
           return _activityVersion;
       }

       public void setActivityVersion(String activityVersion)
       {
           _activityVersion = activityVersion;
       }

       public String getActivityState()
       {
           return _activityState;
       }

       public void setActivityState(String activityState)
       {
           _activityState = activityState;
       }

       public String getActivityRunId()
       {
           return _activityRunId;
       }

       public void setActivityRunId(String activityRunId)
       {
           _activityRunId = activityRunId;
       }
   }


    /**
     * update the activity state of an study
     */
   @Marshal(Marshaller.Jackson)
   @RequiresNoPermission
   @CSRF(CSRF.Method.NONE)
    public class UpdateActivityStateAction extends  ApiAction<PreferencesForm>{

       @Override
       protected ModelAndView handleGet() throws Exception
       {
           getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
           return null;
       }

       @Override
       public ApiResponse execute(PreferencesForm preferencesForm, BindException errors) throws Exception
       {
           ApiSimpleResponse response =  new ApiSimpleResponse();
           boolean isAuthenticated = false;
           try{
               String auth = getViewContext().getRequest().getHeader("auth");
               String userId = getViewContext().getRequest().getHeader("userId");
               List<ParticipantActivities> addParticipantActivitiesList = new ArrayList<ParticipantActivities>();
               if(auth != null && StringUtils.isNotEmpty(auth)){
                   isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                   if(isAuthenticated){
                       if(preferencesForm != null && userId != null && StringUtils.isNotEmpty(userId)){
                           if((preferencesForm.getStudyId() != null && StringUtils.isNotEmpty(preferencesForm.getStudyId())) && (preferencesForm.getActivity() != null && preferencesForm.getActivity().size() > 0)){
                                List<ActivitiesBean> activitiesBeanList = preferencesForm.getActivity();
                                List<ParticipantActivities>   participantActivitiesList = FdahpUserRegWSManager.get().getParticipantActivitiesList(preferencesForm.getStudyId(),userId);
                                for (int i=0;i < activitiesBeanList.size() ; i++){
                                    ActivitiesBean activitiesBean = activitiesBeanList.get(i);
                                    boolean isExists = false;
                                    if(participantActivitiesList != null && participantActivitiesList.size()>0){
                                        for (ParticipantActivities participantActivities : participantActivitiesList)
                                        {
                                            if (participantActivities.getActivityId().equalsIgnoreCase(activitiesBean.getActivityId()))
                                            {
                                                isExists = true;
                                                if(activitiesBean.getActivityVersion()!=null && StringUtils.isNotEmpty(activitiesBean.getActivityVersion()))
                                                    participantActivities.setActivityVersion(activitiesBean.getActivityVersion());
                                                if(activitiesBean.getActivityState()!= null && StringUtils.isNotEmpty(activitiesBean.getActivityState()))
                                                    participantActivities.setActivityState(activitiesBean.getActivityState());
                                                if(activitiesBean.getActivityRunId() != null && StringUtils.isNotEmpty(activitiesBean.getActivityRunId()))
                                                    participantActivities.setActivityRunId(activitiesBean.getActivityRunId());
                                                if(activitiesBean.getBookmarked() != null)
                                                    participantActivities.setBookmark(activitiesBean.getBookmarked());
                                                if(activitiesBean.getActivityRun() != null){
                                                    if(activitiesBean.getActivityRun().getTotal() != null)
                                                        participantActivities.setTotal(activitiesBean.getActivityRun().getTotal());
                                                    if(activitiesBean.getActivityRun().getCompleted() != null)
                                                        participantActivities.setCompleted(activitiesBean.getActivityRun().getCompleted());
                                                    if(activitiesBean.getActivityRun().getMissed() != null)
                                                        participantActivities.setMissed(activitiesBean.getActivityRun().getMissed());
                                                }
                                                addParticipantActivitiesList.add(participantActivities);
                                            }
                                        }

                                    }
                                    if(!isExists){
                                        ParticipantActivities addParticipantActivities = new ParticipantActivities();
                                        if(activitiesBean!= null && StringUtils.isNotEmpty(activitiesBean.getActivityState()))
                                            addParticipantActivities.setActivityState(activitiesBean.getActivityState());
                                        if(activitiesBean.getActivityVersion()!=null && StringUtils.isNotEmpty(activitiesBean.getActivityVersion()))
                                            addParticipantActivities.setActivityVersion(activitiesBean.getActivityVersion());
                                        if(activitiesBean.getActivityId() != null && StringUtils.isNotEmpty(activitiesBean.getActivityId()))
                                            addParticipantActivities.setActivityId(activitiesBean.getActivityId());
                                        if(activitiesBean.getActivityRunId() != null && StringUtils.isNotEmpty(activitiesBean.getActivityRunId()))
                                            addParticipantActivities.setActivityRunId(activitiesBean.getActivityRunId());
                                        if(preferencesForm.getStudyId()!=null && StringUtils.isNotEmpty(preferencesForm.getStudyId()))
                                            addParticipantActivities.setStudyId(preferencesForm.getStudyId());
                                        if(userId!= null && StringUtils.isNotEmpty(userId))
                                            addParticipantActivities.setParticipantId(userId);
                                        if(activitiesBean.getBookmarked() != null)
                                            addParticipantActivities.setBookmark(activitiesBean.getBookmarked());
                                        if(activitiesBean.getActivityRun() != null){
                                            if(activitiesBean.getActivityRun().getTotal() != null)
                                                addParticipantActivities.setTotal(activitiesBean.getActivityRun().getTotal());
                                            if(activitiesBean.getActivityRun().getCompleted() != null)
                                                addParticipantActivities.setCompleted(activitiesBean.getActivityRun().getCompleted());
                                            if(activitiesBean.getActivityRun().getMissed() != null)
                                                addParticipantActivities.setMissed(activitiesBean.getActivityRun().getMissed());
                                        }
                                        addParticipantActivitiesList.add(addParticipantActivities);
                                    }
                                }

                               String message = FdahpUserRegWSManager.get().saveParticipantActivities(addParticipantActivitiesList);
                               if (message.equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue())){
                                   response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                               }else{
                                   FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.FAILURE.getValue(), getViewContext().getResponse());
                                   return null;
                               }
                           }else{
                               FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                               return null;
                           }
                       }else{
                           FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                           return null;
                       }
                   }else{
                       FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                       return null;
                   }
               }else{
                   FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                   return null;
               }
           }catch (Exception e){
               _log.error("UpdateActivityStateAction Action Error",e);
               FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
               return null;
           }
          return response;
       }
   }
   public static class WithDrawForm {

       public String  _studyId;

       public Boolean  _deleteData;

       public String getStudyId()
       {
           return _studyId;
       }

       public void setStudyId(String studyId)
       {
           _studyId = studyId;
       }

       public Boolean getDeleteData()
       {
           return _deleteData;
       }

       public void setDeleteData(Boolean deleteData)
       {
           _deleteData = deleteData;
       }
   }

    /**
     * With draw the user from study
     */
   @RequiresNoPermission
   @CSRF(CSRF.Method.NONE)
    public class WithdrawAction extends  ApiAction<WithDrawForm>{

       @Override
       public ApiResponse execute(WithDrawForm withDrawForm, BindException errors) throws Exception
       {
           ApiSimpleResponse response = new ApiSimpleResponse();
           boolean isAuthenticated = false;
           String message = FdahpUserRegUtil.ErrorCodes.FAILURE.getValue();
           try
           {
               if(isDelete()){
                   String auth = getViewContext().getRequest().getHeader("auth");
                   String userId = getViewContext().getRequest().getHeader("userId");
                   if(auth != null && StringUtils.isNotEmpty(auth)){
                       isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                       if(isAuthenticated){
                            if(withDrawForm != null && null != withDrawForm.getStudyId() && StringUtils.isNotEmpty(withDrawForm.getStudyId()) && null != userId &&  StringUtils.isNotEmpty(userId)){
                               ParticipantStudies participantStudies = FdahpUserRegWSManager.get().getParticipantStudies(withDrawForm.getStudyId(),userId);
                               if(participantStudies != null){
                                    if(participantStudies.getStatus().equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.WITHDRAWN.getValue())){
                                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.NO_DATA_AVAILABLE.getValue(),FdahpUserRegUtil.ErrorCodes.WITHDRAWN_STUDY.getValue(), getViewContext().getResponse());
                                        response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.WITHDRAWN_STUDY.getValue());
                                    }else{
                                        message = FdahpUserRegWSManager.get().withDrawStudy(withDrawForm.getStudyId(),userId,withDrawForm.getDeleteData());
                                        if(message.equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue())){
                                            response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                                            FdahpUserRegWSManager.addAuditEvent(userId,"With draw from study","User withdrawn from study "+withDrawForm.getStudyId()+".","FdaStudyAuditEvent",getViewContext().getContainer().getId());
                                        }else{
                                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.FAILURE.getValue(), getViewContext().getResponse());
                                            return null;
                                        }
                                    }
                               }else{
                                   FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.FAILURE.getValue(), getViewContext().getResponse());
                                   return null;
                               }
                           }else{
                               FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                               return null;
                           }

                       }else{
                           FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                           return null;
                       }

                   }else{
                       FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                       return null;
                   }
               }else{
                   getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the DELETE method when calling this action.");
                   return null;
               }
           }catch (Exception e){
               _log.error("Withdraw Action Error:",e);
               FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
               return null;
           }
           return response;
       }
   }

    /**
     * Get the consent pdf of an user
     */
   @RequiresNoPermission
   @CSRF(CSRF.Method.NONE)
    public class ConsentPDFAction extends ApiAction<ActivityForm>{

       @Override
       public ApiResponse execute(ActivityForm activityForm, BindException errors) throws Exception
       {
           ApiSimpleResponse response = new ApiSimpleResponse();
           boolean isAuthenticated = false;
           try{
               String auth = getViewContext().getRequest().getHeader("auth");
               String userId = getViewContext().getRequest().getHeader("userId");
               String consentVersion = getViewContext().getRequest().getParameter("consentVersion");
               String studyId = getViewContext().getRequest().getParameter("studyId");
               getViewContext().getRequest().getParameter("studyId");

               if(auth != null && StringUtils.isNotEmpty(auth)){
                   isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                   if(isAuthenticated){
                       if(studyId != null && StringUtils.isNoneBlank(studyId) && userId != null && StringUtils.isNotEmpty(userId)){
                           StudyConsent studyConsent = FdahpUserRegWSManager.get().getStudyConsent(userId,studyId,consentVersion);
                           if(studyConsent != null){
                               JSONObject jsonObject  = new JSONObject();
                               if(studyConsent.getVersion() != null)
                                   jsonObject.put("version",studyConsent.getVersion());
                               if(studyConsent.getPdf() != null)
                                   jsonObject.put("content",studyConsent.getPdf());
                               jsonObject.put("type", "application/pdf");
                               response.put("consent",jsonObject);
                               ParticipantStudies participantStudies = FdahpUserRegWSManager.get().getParticipantStudies(studyId,userId);
                               if(participantStudies != null){
                                   response.put("sharing",participantStudies.getSharing());
                               }
                               response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                            }else{
                                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.NO_DATA_AVAILABLE.getValue(),FdahpUserRegUtil.ErrorCodes.NO_DATA_AVAILABLE.getValue(), getViewContext().getResponse());
                                return  null;
                            }
                       }else{
                           FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                           return null;
                       }
                   }else{
                       FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                       return null;
                   }
               }else{
                   FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                   return null;
               }
           }catch (Exception e){
               _log.error("ConsentPDFAction Action Error",e);
               FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
               return null;
           }
           return response;
       }
   }

    /**
     * Delete of an user
     */
   @RequiresNoPermission
   @CSRF(CSRF.Method.NONE)
    public class DeleteAccountAction extends ApiAction{

       @Override
       public Object execute(Object o, BindException errors) throws Exception
       {
           ApiSimpleResponse response = new ApiSimpleResponse();
           boolean isAuthenticated = false;
           try{
               String userId = getViewContext().getRequest().getHeader("userId");
               String auth = getViewContext().getRequest().getHeader("auth");
               if((userId != null && StringUtils.isNotEmpty(userId)) && (auth != null && StringUtils.isNotEmpty(auth))){
                   isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                   if(isAuthenticated){
                        String message = FdahpUserRegWSManager.get().deleteAccount(userId);
                        if(message.equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue())){
                            response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.FAILURE.getValue(), getViewContext().getResponse());
                            return null;
                        }
                   }else{
                       FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                       return null;
                   }
               }else{
                   FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                   return null;
               }
           }catch (Exception e){
               _log.error("Delete Account Action:",e);
               FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
               return null;
           }
           return response;
       }
   }
   public static class DeactivateForm {

        public List<String> _deleteData;

       public List<String> getDeleteData()
       {
           return _deleteData;
       }

       public void setDeleteData(List<String> deleteData)
       {
           _deleteData = deleteData;
       }
   }

    /**
     * Deactivation of an user
     */
    @Marshal(Marshaller.Jackson)
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class DeactivateAction extends ApiAction<DeactivateForm>{

        @Override
        public Object execute(DeactivateForm deactivateForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            boolean isAuthenticated = false;
            String message = FdahpUserRegUtil.ErrorCodes.FAILURE.getValue();
            try
            {
                if(isDelete()){
                    String auth = getViewContext().getRequest().getHeader("auth");
                    String userId = getViewContext().getRequest().getHeader("userId");

                    if(auth != null && StringUtils.isNotEmpty(auth)){
                        isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                        if(isAuthenticated){
                            if(null != userId && StringUtils.isNotEmpty(userId)){
                                message = FdahpUserRegWSManager.get().deActivate(userId,deactivateForm);
                                if(message.equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue())){
                                    FdahpUserRegWSManager.addAuditEvent(userId,"ACCOUNT DELETE","User account deleted. (User ID = "+userId+") ","FdaUserAuditEvent",getViewContext().getContainer().getId());
                                    response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                                }else{
                                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.FAILURE.getValue(), getViewContext().getResponse());
                                    return null;
                                }
                            }else{
                                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                                return null;
                            }
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }

                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the DELETE method when calling this action.");
                    return null;
                }

            }catch (Exception e){
                _log.error("Deactivate  Action Error:",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    /**
     * update the study sate
     */
    @Marshal(Marshaller.Jackson)
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class UpdateStudyStateAction extends  ApiAction<PreferencesForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public ApiResponse execute(PreferencesForm preferencesForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response  = new ApiSimpleResponse();
            List<ParticipantStudies> addParticipantStudiesList = new ArrayList<ParticipantStudies>();
            try{
                String auth = getViewContext().getRequest().getHeader("auth");
                String userId = getViewContext().getRequest().getHeader("userId");
                boolean isAuthenticated = false;
                if(auth != null && StringUtils.isNotEmpty(auth)){
                    isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                    if(isAuthenticated){
                        if(preferencesForm != null && userId != null && StringUtils.isNotEmpty(userId)){
                            if(preferencesForm.getStudies() != null && preferencesForm.getStudies().size() > 0)
                            {
                                List<StudiesBean> studiesBeenList = preferencesForm.getStudies();
                                List<ParticipantStudies> existParticipantStudies = FdahpUserRegWSManager.get().getParticipantStudiesList(userId);

                                for (int i = 0; i < studiesBeenList.size(); i++)
                                {
                                    StudiesBean studiesBean = studiesBeenList.get(i);
                                    boolean isExists = false;
                                    if (existParticipantStudies != null && existParticipantStudies.size() > 0)
                                    {
                                        for (ParticipantStudies participantStudies : existParticipantStudies)
                                        {

                                            if (studiesBean.getStudyId().equalsIgnoreCase(participantStudies.getStudyId()))
                                            {
                                                isExists = true;
                                                if(participantStudies.getStatus() != null && participantStudies.getStatus().equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.YET_TO_JOIN.getValue())){
                                                    participantStudies.setEnrolledDate(FdahpUserRegUtil.getCurrentUtilDateTime());
                                                }
                                                if(studiesBean.getStatus() != null && StringUtils.isNotEmpty(studiesBean.getStatus())){
                                                    participantStudies.setStatus(studiesBean.getStatus());
                                                    if(studiesBean.getStatus().equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.IN_PROGRESS.getValue())){
                                                        List<ParticipantActivities>   participantActivitiesList = FdahpUserRegWSManager.get().getParticipantActivitiesList(studiesBean.getStudyId(),userId);
                                                        if(participantActivitiesList != null && participantActivitiesList.size()>0){
                                                            for (ParticipantActivities participantActivities : participantActivitiesList)
                                                            {
                                                                participantActivities.setActivityVersion(null);
                                                                participantActivities.setActivityState(null);
                                                                participantActivities.setActivityRunId(null);
                                                                participantActivities.setBookmark(false);
                                                                participantActivities.setTotal(0);
                                                                participantActivities.setCompleted(0);
                                                                participantActivities.setMissed(0);
                                                            }
                                                            FdahpUserRegWSManager.get().saveParticipantActivities(participantActivitiesList);
                                                        }
                                                        participantStudies.setEnrolledDate(FdahpUserRegUtil.getCurrentUtilDateTime());
                                                    }
                                                }
                                                if (studiesBean.getBookmarked() != null)
                                                    participantStudies.setBookmark(studiesBean.getBookmarked());
                                                if (studiesBean.getCompletion() != null)
                                                    participantStudies.setCompletion(studiesBean.getCompletion());
                                                if (studiesBean.getAdherence() != null)
                                                    participantStudies.setAdherence(studiesBean.getAdherence());
                                                if(studiesBean.getParticipantId() != null &&  StringUtils.isNotEmpty(studiesBean.getParticipantId()))
                                                    participantStudies.setParticipantId(studiesBean.getParticipantId());
                                                addParticipantStudiesList.add(participantStudies);
                                            }
                                        }
                                    }
                                    if (!isExists)
                                    {
                                        ParticipantStudies participantStudies = new ParticipantStudies();
                                        if (studiesBean.getStudyId() != null && StringUtils.isNotEmpty(studiesBean.getStudyId()))
                                            participantStudies.setStudyId(studiesBean.getStudyId());
                                        if (studiesBean.getStatus() != null && StringUtils.isNotEmpty(studiesBean.getStatus()))
                                        {
                                            participantStudies.setStatus(studiesBean.getStatus());
                                            if(studiesBean.getStatus().equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.IN_PROGRESS.getValue())){
                                                participantStudies.setEnrolledDate(FdahpUserRegUtil.getCurrentUtilDateTime());
                                            }

                                        }else{
                                            participantStudies.setStatus(FdahpUserRegUtil.ErrorCodes.YET_TO_JOIN.getValue());
                                        }
                                        if (studiesBean.getBookmarked() != null)
                                            participantStudies.setBookmark(studiesBean.getBookmarked());
                                        if (userId != null && StringUtils.isNotEmpty(userId))
                                            participantStudies.setUserId(userId);
                                        if (studiesBean.getCompletion() != null)
                                            participantStudies.setCompletion(studiesBean.getCompletion());
                                        if (studiesBean.getAdherence() != null)
                                            participantStudies.setAdherence(studiesBean.getAdherence());
                                        if(studiesBean.getParticipantId() != null &&  StringUtils.isNotEmpty(studiesBean.getParticipantId()))
                                            participantStudies.setParticipantId(studiesBean.getParticipantId());
                                        addParticipantStudiesList.add(participantStudies);
                                    }
                                }
                                FdahpUserRegWSManager.get().saveParticipantStudies(addParticipantStudiesList);
                            }
                            response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }
            }catch (Exception e){
                _log.error("UpdateStudyState Action Error :",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    /**
     * Get the study state of an user
     */
    @Marshal(Marshaller.Jackson)
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class StudyStateAction extends  ApiAction<UserForm>{

        @Override
        public ApiResponse execute(UserForm userForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            boolean isAuthenticated = false;
            try{
                String auth = getViewContext().getRequest().getHeader("auth");
                String userId =  getViewContext().getRequest().getHeader("userId");
                if(auth != null && StringUtils.isNotEmpty(auth)){
                    isAuthenticated = FdahpUserRegWSManager.get().validatedAuthKey(auth);
                    if(isAuthenticated){
                        if(userId != null && StringUtils.isNotEmpty(userId)){
                            response = FdahpUserRegWSManager.get().getPreferences(userId);
                        }else{
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_101.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_AUTH_CODE.getValue(), FdahpUserRegUtil.ErrorCodes.SESSION_EXPIRED_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }
            }catch (Exception e){
                _log.error("StudyStateAction Action Error",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;

            }
            return response;
        }
    }

    public static class NotificationForm {

        public List<NotificationBean>  _notifications;

        public List<NotificationBean> getNotifications()
        {
            return _notifications;
        }

        public void setNotifications(List<NotificationBean> notifications)
        {
            _notifications = notifications;
        }
    }

    /**
     * Send the push notifications
     */
    @Marshal(Marshaller.Jackson)
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class SendNotificationAction extends ApiAction<NotificationForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public ApiResponse execute(NotificationForm notificationForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            Map<String,Map<String,JSONArray>> studiesMap = null;
            try{
                if(notificationForm != null && notificationForm.getNotifications() != null && !notificationForm.getNotifications().isEmpty()){
                    if(!notificationForm.getNotifications().isEmpty()){
                        HashSet<String> studySet = new HashSet<>();
                        studiesMap = new HashMap<>();
                        Map<String,JSONArray> allDeviceTokens = FdahpUserRegWSManager.get().getDeviceTokenOfAllUsers();

                        for (NotificationBean notificationBean : notificationForm.getNotifications()){
                            if(notificationBean.getNotificationType().equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.STUDY_LEVEL.getValue())){
                                studySet.add(notificationBean.getCustomStudyId());
                            }
                        }
                        if(studySet != null && !studySet.isEmpty()){
                            studiesMap = FdahpUserRegWSManager.get().getStudyLevelDeviceToken("'"+StringUtils.join(studySet,"','")+"'");
                           _log.info("studiesMap:"+studiesMap);
                        }
                        for (NotificationBean notificationBean : notificationForm.getNotifications()){
                            if(notificationBean.getNotificationType().equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.GATEWAY_LEVEL.getValue())){
                                notificationBean.setNotificationType(FdahpUserRegUtil.ErrorCodes.GATEWAY.getValue());
                                if(allDeviceTokens.get(FdahpUserRegUtil.ErrorCodes.DEVICE_ANDROID.getValue()) != null){
                                    notificationBean.setDeviceToken(allDeviceTokens.get(FdahpUserRegUtil.ErrorCodes.DEVICE_ANDROID.getValue()));
                                    if(notificationBean.getDeviceToken() != null && notificationBean.getDeviceToken().length() > 0){
                                        pushFCMNotification(notificationBean);
                                    }

                                }
                                if(allDeviceTokens.get(FdahpUserRegUtil.ErrorCodes.DEVICE_IOS.getValue()) != null){
                                    notificationBean.setDeviceToken(allDeviceTokens.get(FdahpUserRegUtil.ErrorCodes.DEVICE_IOS.getValue()));
                                    if(notificationBean.getDeviceToken() != null && notificationBean.getDeviceToken().length() > 0){
                                        FdahpUserRegUtil.pushNotification(notificationBean);
                                    }

                                }
                            }else if(notificationBean.getNotificationType().equalsIgnoreCase(FdahpUserRegUtil.ErrorCodes.STUDY_LEVEL.getValue())){
                                Map<String,JSONArray> deviceTokensMap = studiesMap.get(notificationBean.getCustomStudyId());
                                notificationBean.setNotificationType(FdahpUserRegUtil.ErrorCodes.STUDY.getValue());
                                if(deviceTokensMap != null){
                                    if(deviceTokensMap.get(FdahpUserRegUtil.ErrorCodes.DEVICE_ANDROID.getValue()) != null){
                                        notificationBean.setDeviceToken(deviceTokensMap.get(FdahpUserRegUtil.ErrorCodes.DEVICE_ANDROID.getValue()));
                                        if(notificationBean.getDeviceToken() != null && notificationBean.getDeviceToken().length() > 0){
                                            pushFCMNotification(notificationBean);
                                        }
                                    }
                                    if(deviceTokensMap.get(FdahpUserRegUtil.ErrorCodes.DEVICE_IOS.getValue()) != null){
                                        notificationBean.setDeviceToken(deviceTokensMap.get(FdahpUserRegUtil.ErrorCodes.DEVICE_IOS.getValue()));
                                        if(notificationBean.getDeviceToken() != null && notificationBean.getDeviceToken().length() > 0){
                                            FdahpUserRegUtil.pushNotification(notificationBean);
                                        }
                                    }
                                }
                            }

                        }
                        response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }

                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }
            }catch (Exception e){
                _log.error("SendNotificationAction Action Error",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.getValue(),FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return  response;
        }
    }

    /**
     * Andriod push notification
     * @param notification
     */
    public void pushFCMNotification(NotificationBean notification) {

       try{
            String authKey = (String) configProp.get("AUTH_KEY_FCM");   // You FCM AUTH key
            String FMCurl = (String) configProp.get("API_URL_FCM");


            URL url = new URL(FMCurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization","key="+authKey);
            conn.setRequestProperty("Content-Type","application/json");

            JSONObject json = new JSONObject();

            _log.info("notification.getDeviceToken():"+notification.getDeviceToken());
            json.put("registration_ids",notification.getDeviceToken());
            json.put("priority","high");

            JSONObject dataInfo = new JSONObject();
            _log.info("notification.getNotificationSubType():"+notification.getNotificationSubType());
            dataInfo.put("subtype", notification.getNotificationSubType());
            dataInfo.put("type", notification.getNotificationType());
            dataInfo.put("title", notification.getNotificationTitle());
            dataInfo.put("message", notification.getNotificationText());
            if(notification.getCustomStudyId() != null && StringUtils.isNotEmpty(notification.getCustomStudyId())){
                dataInfo.put("studyId", notification.getCustomStudyId());
            }
            json.put("data", dataInfo);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();
            conn.getInputStream();
        }catch (Exception e){
            _log.error("pushFCMNotification Action Error",e);
        }

    }

    /**
     * Generate the consent doc as pdf file
     */
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class GenerateTheFileAction extends ApiAction<StudyConsent>{

        @Override
        public ApiResponse execute(StudyConsent StudyConsent, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            List<StudyConsent> studyConsentList = FdahpUserRegWSManager.get().getStudyConsentList();
            FileContentService fileContentService = ServiceRegistry.get().getService(FileContentService.class);
            //File root = fileContentService.getDefaultRoot(getViewContext().getContainer(), true);
            File root = fileContentService.getFileRoot(getViewContext().getContainer(), FileContentService.ContentType.files);
            if(!root.exists())
                root.mkdirs();
            _log.info("root:"+root);
            File rootModule = null;
            if (root != null)
            {
                 rootModule = new File(root, FdahpUserRegWSModule.NAME);
                 if (!rootModule.exists())
                    rootModule.mkdirs();
                _log.info(rootModule);

            }
            _log.info(studyConsentList.size());
            if(studyConsentList != null && studyConsentList.size() > 0){
                for (StudyConsent studyConsent : studyConsentList){
                    if(rootModule != null){
                        File dir = new File(rootModule, studyConsent.getStudyId());
                        if(!dir.exists())
                            dir.mkdir();
                        String fileName = FdahpUserRegUtil.getStandardFileName(studyConsent.getStudyId(),studyConsent.getUserId(),studyConsent.getVersion());
                        _log.info(fileName);
                        try {
                            byte[] decodedBytes;
                            FileOutputStream fop;
                            decodedBytes =  Base64.getDecoder().decode(studyConsent.getPdf());
                            File file = new File(dir,fileName);
                            fop = new FileOutputStream(file);
                            fop.write(decodedBytes);
                            fop.flush();
                            fop.close();
                        } catch (Exception e) {
                            _log.error("Error..",e);
                            response.put("message","Fail");
                        }
                    }

                }
            }
            response.put("message","SUCESS");
            response.put("status", studyConsentList.size()+" Files Created");
            return response;
        }
    }

    /**
     * saving the generated the consent docmunet in folder path
     * @param studyConsent
     * @return
     */
    public String saveConsentDocument(StudyConsent studyConsent){
        String fileName="";
        try{
            FileContentService fileContentService = ServiceRegistry.get().getService(FileContentService.class);
            File root = fileContentService.getFileRoot(getViewContext().getContainer(), FileContentService.ContentType.files);
            if(!root.exists())
                root.mkdirs();
            File rootModule = null;
            _log.info("root:"+root);
            if (root != null)
            {
                rootModule = new File(root, FdahpUserRegWSModule.NAME);
                if (!rootModule.exists())
                    rootModule.mkdirs();
            }
            _log.info("rootModule:"+rootModule);
            if(rootModule != null){
                File dir = new File(rootModule, studyConsent.getStudyId());
                if(!dir.exists())
                    dir.mkdir();
                fileName = FdahpUserRegUtil.getStandardFileName(studyConsent.getStudyId(),studyConsent.getUserId(),studyConsent.getVersion());
                _log.info(fileName);
                try {
                    byte[] decodedBytes;
                    FileOutputStream fop;
                    decodedBytes =  Base64.getDecoder().decode(studyConsent.getPdf().replaceAll("\n", ""));
                    File file = new File(dir,fileName);
                    fop = new FileOutputStream(file);
                    fop.write(decodedBytes);
                    fop.flush();
                    fop.close();
                } catch (Exception e) {
                    _log.error("FdahpUserRegWSController saveConsentDocument:",e);
                }
            }
        }catch (Exception e){
            _log.error("FdahpUserRegWSController saveConsentDocument:",e);
        }
        return fileName;
    }

    public static class RefreshTokenForm{

        public  String _refreshToken;

        public String getRefreshToken()
        {
            return _refreshToken;
        }

        public void setRefreshToken(String refreshToken)
        {
            _refreshToken = refreshToken;
        }
    }

    /**
     * Refresh of an auth key
     */
    @RequiresNoPermission
	@CSRF(CSRF.Method.NONE)
    public class RefreshTokenAction extends ApiAction<RefreshTokenForm>{

        @Override
        protected ModelAndView handleGet() throws Exception
        {
            getViewContext().getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "You must use the POST method when calling this action.");
            return null;
        }

        @Override
        public ApiResponse execute(RefreshTokenForm refreshTokenForm, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            AuthInfo existedAuthInfo = null;
            try{
                if(refreshTokenForm != null){
                    if((refreshTokenForm.getRefreshToken() != null && StringUtils.isNotEmpty(refreshTokenForm.getRefreshToken()))){
                        existedAuthInfo = FdahpUserRegWSManager.get().getAuthInfoByRefreshToken(refreshTokenForm.getRefreshToken());
                        if(null != existedAuthInfo){
                            AuthInfo authInfo = FdahpUserRegWSManager.get().saveAuthInfo(existedAuthInfo.getParticipantId(),false);
                            if(authInfo != null){
                                response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
                                response.put("userId",authInfo.getParticipantId());
                                response.put("auth",authInfo.getAuthKey());
                                response.put("refreshToken",authInfo.getRefreshToken());
                            }
                        }else{
                            FdahpUserRegWSManager.addAuditEvent(null,"FAILED RefreshToken IN","Wrong RefreshToken. Which is not existed.","FdaUserAuditEvent",getViewContext().getContainer().getId());
                            FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_103.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_REFRESHTOKEN.name(), FdahpUserRegUtil.ErrorCodes.INVALID_REFRESHTOKEN.getValue(), getViewContext().getResponse());
                            return null;
                        }
                    }else{
                        FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.name(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                        return null;
                    }
                }else{
                    FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_102.getValue(),FdahpUserRegUtil.ErrorCodes.INVALID_INPUT.name(), FdahpUserRegUtil.ErrorCodes.INVALID_INPUT_ERROR_MSG.getValue(), getViewContext().getResponse());
                    return null;
                }


            }catch (Exception e){
                _log.error("Login Action:",e);
                FdahpUserRegUtil.getFailureResponse(FdahpUserRegUtil.ErrorCodes.STATUS_104.getValue(),FdahpUserRegUtil.ErrorCodes.UNKNOWN.name(), FdahpUserRegUtil.ErrorCodes.CONNECTION_ERROR_MSG.getValue(), getViewContext().getResponse());
                return null;
            }
            return response;
        }
    }

    /**
     * saving the consent documents into particular study folder
     * @param studyConsent
     * @return
     */
    public String saveStudyConsentDocument(StudyConsent studyConsent){
        _log.info("FdahpUserRegWSController saveStudyConsentDocument starts");
        String fileName="";
        try{
            FileContentService fileContentService = ServiceRegistry.get().getService(FileContentService.class);
            Container availableContainer = null;
            boolean isAvailable= false;
            Module module =  ModuleLoader.getInstance().getModule(FdahpUserRegWSModule.NAME);
            Set<Container> all = ContainerManager.getAllChildren(ContainerManager.getRoot());

            ModuleProperty mp = module.getModuleProperties().get("StudyId");
            String postedStudyId = studyConsent.getStudyId();
            File root = null;
            for (Container c : all)
            {
                String studyId = mp.getValueContainerSpecific(c);
                if (postedStudyId.equalsIgnoreCase(studyId))
                {
                    isAvailable = true;
                    root = fileContentService.getFileRoot(c, FileContentService.ContentType.files);
                    break;
                }
                
            }
            _log.info("isAvailable:"+isAvailable);
            _log.info("root:"+root);
            if(isAvailable){
                if(!root.exists())
                    root.mkdirs();
                fileName = FdahpUserRegUtil.getStandardFileName(studyConsent.getStudyId(),studyConsent.getUserId(),studyConsent.getVersion());
                _log.info(fileName);
                try {
                    byte[] decodedBytes;
                    FileOutputStream fop;
                    decodedBytes =  Base64.getDecoder().decode(studyConsent.getPdf().replaceAll("\n", ""));
                    File file = new File(root,fileName);
                    fop = new FileOutputStream(file);
                    fop.write(decodedBytes);
                    fop.flush();
                    fop.close();
                } catch (Exception e) {
                    _log.error("FdahpUserRegWSController saveStudyConsentDocument:",e);
                }
            }else{
                saveConsentDocument(studyConsent);
            }

        }catch (Exception e){
            _log.error("FdahpUserRegWSController saveStudyConsentDocument:",e);
        }
        _log.info("FdahpUserRegWSController saveStudyConsentDocument Exit");
        return fileName;
    }


}