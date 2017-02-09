/*
 * Copyright (c) 2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.labkey.fdahpuserregws;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import org.json.JSONObject;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.gwt.client.AuditBehaviorType;
import org.labkey.api.query.FieldKey;
import org.labkey.fdahpuserregws.bean.ActivitiesBean;

import org.labkey.fdahpuserregws.bean.ParticipantForm;
import org.labkey.fdahpuserregws.bean.ParticipantInfoBean;
import org.labkey.fdahpuserregws.bean.ProfileBean;
import org.labkey.fdahpuserregws.bean.SettingsBean;
import org.labkey.fdahpuserregws.bean.StudiesBean;
import org.labkey.fdahpuserregws.model.AuthInfo;
import org.labkey.fdahpuserregws.model.FdahpUserRegUtil;
import org.labkey.fdahpuserregws.model.ParticipantActivities;
import org.labkey.fdahpuserregws.model.ParticipantDetails;
import org.labkey.fdahpuserregws.model.ParticipantStudies;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FdahpUserRegWSManager
{
    private static final FdahpUserRegWSManager _instance = new FdahpUserRegWSManager();

    private FdahpUserRegWSManager()
    {
        // prevent external construction with a private default constructor
    }

    public static FdahpUserRegWSManager get()
    {
        return _instance;
    }

    private static final Logger _log = Logger.getLogger(FdahpUserRegWSManager.class);

    public ParticipantDetails saveParticipant(Container container, ParticipantDetails participant){
        DbScope dbScope = FdahpUserRegWSSchema.getInstance().getSchema().getScope();
        ParticipantDetails addParticipant = null;
        DbScope.Transaction transaction = dbScope.ensureTransaction();
        try{
            TableInfo table = FdahpUserRegWSSchema.getInstance().getParticipantDetails();
            table.setAuditBehavior(AuditBehaviorType.DETAILED);
            if(null!=participant && participant.getId() == null){
                addParticipant = Table.insert(null,table, participant);
            }else{
                addParticipant = Table.update(null, table, participant, participant.getId());
            }

        }catch (Exception e){
            _log.error("saveParticipant:",e);
        }
        transaction.commit();
        return addParticipant;
    }

    public ParticipantDetails getParticipantDetails(Container container,int id){
        SimpleFilter filter = new SimpleFilter();
        filter.addCondition(FieldKey.fromParts("Id"), id);
        return new TableSelector(FdahpUserRegWSSchema.getInstance().getParticipantDetails(), filter, null).getObject(ParticipantDetails.class);
    }

    public List<ParticipantDetails> getParticipantDetailsByEmail(Container container, String email){
        SimpleFilter filter = new SimpleFilter();
        filter.addCondition(FieldKey.fromParts("Email"), email);
        return new TableSelector(FdahpUserRegWSSchema.getInstance().getParticipantDetails(),filter,null).getArrayList(ParticipantDetails.class);
    }

    public AuthInfo saveAuthInfo(Container container, Integer userId){
        DbScope dbScope = FdahpUserRegWSSchema.getInstance().getSchema().getScope();
        ParticipantDetails addParticipant = null;
        DbScope.Transaction transaction = dbScope.ensureTransaction();
        AuthInfo authInfo = null;
        try{
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("ParticipantId"), userId);
            authInfo = new TableSelector(FdahpUserRegWSSchema.getInstance().getAuthInfo(),filter,null).getObject(AuthInfo.class);
            String authKey = "0";
            authKey = RandomStringUtils.randomNumeric(9);
            TableInfo table = FdahpUserRegWSSchema.getInstance().getAuthInfo();
            table.setAuditBehavior(AuditBehaviorType.DETAILED);
            System.out.println("table:"+table.getAuditBehavior());
            if(null !=authInfo){
                authInfo.setAuthKey(authKey);
                authInfo.setDeviceToken("");
                authInfo.setDeviceType("");
                authInfo.setModifiedOn(new Date());
                Table.update(null,table, authInfo,authInfo.getAuthId());
            }else{
                authInfo = new AuthInfo();
                authInfo.setAuthKey(authKey);
                authInfo.setDeviceToken("");
                authInfo.setDeviceType("");
                authInfo.setParticipantId(userId);
                authInfo.setCreatedOn(new Date());
                Table.insert(null,table, authInfo);

            }

        }catch (Exception e){
            _log.error("saveAuthInfo:",e);
        }
        transaction.commit();
        return authInfo;
    }
    public boolean validatedAuthKey(Container container,String authKey,Integer participantId){
        boolean isAuthenticated = false;
        try{
            AuthInfo authInfo = null;
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("AuthKey"), authKey);
            filter.addCondition(FieldKey.fromParts("ParticipantId"), participantId);
            authInfo  = new TableSelector(FdahpUserRegWSSchema.getInstance().getAuthInfo(),filter,null).getObject(AuthInfo.class);
            if(authInfo != null){
                isAuthenticated = true;
            }
        }catch (Exception e){
            _log.error("FdahpUserRegWSManger validatedAuthKey ()",e);
        }

        return isAuthenticated;
    }

    public ParticipantForm signingParticipant(Container container, String email, String password){
        ParticipantForm participantForm = null;
        ParticipantDetails participantDetails = null;
        try{
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("Email"), email);
            filter.addCondition(FieldKey.fromParts("Password"), FdahpUserRegUtil.getEncryptedString(password));
            participantDetails = new TableSelector(FdahpUserRegWSSchema.getInstance().getParticipantDetails(),filter,null).getObject(ParticipantDetails.class);
            if(null != participantDetails){
                participantForm = new ParticipantForm();
                AuthInfo authInfo = saveAuthInfo(container,participantDetails.getId());
                if(authInfo != null){
                    participantForm.setAuth(authInfo.getAuthKey());
                }
                participantForm.setUserId(participantDetails.getId());
                participantForm.setFirstName(participantDetails.getFirstName());
                participantForm.setStatus(participantDetails.getStatus());
                participantForm.setLastName(participantDetails.getLastName());
                participantForm.setEmail(participantDetails.getEmail());
            }
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager signingParticipant()",e);
        }
        return  participantForm;
    }

    public ParticipantDetails getParticipantDetailsByEmail(String email){
        ParticipantDetails participantDetails = null;
        try{
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("Email"), email);
            participantDetails =  new TableSelector(FdahpUserRegWSSchema.getInstance().getParticipantDetails(),filter,null).getObject(ParticipantDetails.class);
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager getParticipantDetailsByEmail()",e);
        }
        return  participantDetails;
    }

    public String signout(Integer userId){
        String message = FdahpUserRegUtil.ErrorCodes.FAILURE.getValue();
        try{
            DbSchema schema = FdahpUserRegWSSchema.getInstance().getSchema();
            TableInfo authInfo = FdahpUserRegWSSchema.getInstance().getAuthInfo();
            authInfo.setAuditBehavior(AuditBehaviorType.DETAILED);
            SqlExecutor executor = new SqlExecutor(schema);
            SQLFragment sqlUpdateVisitDates = new SQLFragment();

            sqlUpdateVisitDates.append("UPDATE ").append(authInfo.getSelectName()).append("\n")
                    .append("SET AuthKey = 0, ModifiedOn='"+FdahpUserRegUtil.getCurrentDateTime()+"'")
                    .append(" WHERE ParticipantId = "+userId);
            int execute = executor.execute(sqlUpdateVisitDates);
            if (execute >= 0){
                message = FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue();
            }
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager signout error:",e);
        }
        return message;
    }

    public ApiSimpleResponse getParticipantDetails(Integer userId){
        JSONObject jsonObject  = new JSONObject();
        ApiSimpleResponse response  = new ApiSimpleResponse();
        try{
            ParticipantDetails participantDetails = getParticipantDetails(null,userId);
            if(participantDetails != null){
                ProfileBean profileBean = new ProfileBean();
                if(participantDetails.getFirstName()!=null)
                    profileBean.setFirstName(participantDetails.getFirstName());
                if(participantDetails.getLastName() != null)
                    profileBean.setLastName(participantDetails.getLastName());
                if(participantDetails.getEmail() != null)
                    profileBean.setEmailId(participantDetails.getEmail());
                response.put(FdahpUserRegUtil.ErrorCodes.PROFILE.getValue(),profileBean);
                SettingsBean settingsBean = new SettingsBean();
                if(participantDetails.getLocalNotificationFlag() != null)
                    settingsBean.setLocalNotifications(participantDetails.getLocalNotificationFlag());
                if(participantDetails.getUsePassCode())
                    settingsBean.setPasscode(participantDetails.getUsePassCode());
                if(participantDetails.getRemoteNotificationFlag()!=null)
                    settingsBean.setRemoteNotifications(participantDetails.getRemoteNotificationFlag());
                if(participantDetails.getTouchId() != null)
                    settingsBean.setTouchId(participantDetails.getTouchId());
                response.put(FdahpUserRegUtil.ErrorCodes.SETTINGS.getValue(),settingsBean);
                response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
            }
            List<ParticipantStudies> participantStudiesList = getParticipantStudiesList(userId);
            if(participantStudiesList != null && participantStudiesList.size() > 0){
                List<ParticipantInfoBean> participantInfoBeanList = new ArrayList<ParticipantInfoBean>();
                for (ParticipantStudies participantStudies : participantStudiesList){
                    if(participantStudies.getAppToken() != null)
                    {
                        ParticipantInfoBean participantInfoBean = new ParticipantInfoBean();
                        if(participantStudies.getParticipantId() != null)
                            participantInfoBean.setParticipantId(String.valueOf(participantStudies.getParticipantId()));
                        if(participantStudies.getStudyId() != null)
                            participantInfoBean.setStudyId(String.valueOf(participantStudies.getStudyId()));
                        if(participantStudies.getAppToken() != null)
                            participantInfoBean.setAppToken(participantStudies.getAppToken());
                        participantInfoBeanList.add(participantInfoBean);
                    }
                }
                response.put(FdahpUserRegUtil.ErrorCodes.PARTICIPANTINFO.getValue(),participantInfoBeanList);
            }
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager getParticipantDetails error:",e);
        }
        return  response;
    }

    public List<ParticipantStudies> getParticipantStudiesList(Integer userId){
        List<ParticipantStudies> participantStudiesList = null;
        try{
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("UserId"), userId);
            participantStudiesList = new TableSelector(FdahpUserRegWSSchema.getInstance().getParticipantStudies(),filter,null).getArrayList(ParticipantStudies.class);
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager getParticipantStudiesList error:",e);
        }
        return  participantStudiesList;
    }

    public List<ParticipantActivities> getParticipantActivitiesList(Integer userId){
        List<ParticipantActivities> participantActivitiesList = null;
        try{
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("ParticipantId"), userId);
            participantActivitiesList = new TableSelector(FdahpUserRegWSSchema.getInstance().getParticipantActivities(),filter,null).getArrayList(ParticipantActivities.class);
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager getParticipantActivitiesList error :",e);
        }
        return participantActivitiesList;
    }

    public String saveParticipantStudies(List<ParticipantStudies> participantStudiesList){
        String message = FdahpUserRegUtil.ErrorCodes.FAILURE.getValue();
        DbScope dbScope = FdahpUserRegWSSchema.getInstance().getSchema().getScope();
        DbScope.Transaction transaction = dbScope.ensureTransaction();
        try{
            TableInfo table = FdahpUserRegWSSchema.getInstance().getParticipantStudies();
            table.setAuditBehavior(AuditBehaviorType.DETAILED);
            for(ParticipantStudies participantStudies : participantStudiesList){
                if(participantStudies.getId() != null ){
                    Table.update(null,table,participantStudies,participantStudies.getId());
                }else{
                    Table.insert(null, table, participantStudies);
                }
            }

            if(participantStudiesList.size() > 0){
                message = FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue();
            }
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager saveParticipantStudies error :",e);
        }
        transaction.commit();
        return message;
    }

    public String saveParticipantActivities(List<ParticipantActivities> participantActivitiesList){
        String message = FdahpUserRegUtil.ErrorCodes.FAILURE.getValue();
        DbScope dbScope = FdahpUserRegWSSchema.getInstance().getSchema().getScope();
        DbScope.Transaction transaction = dbScope.ensureTransaction();
        try{
            TableInfo table = FdahpUserRegWSSchema.getInstance().getParticipantActivities();
            table.setAuditBehavior(AuditBehaviorType.DETAILED);
            for (ParticipantActivities participantActivities : participantActivitiesList)
                if(participantActivities.getId() != null)
                    Table.update(null,table,participantActivities,participantActivities.getId());
                else
                    Table.insert(null,table,participantActivities);
            if (participantActivitiesList.size() > 0){
                message = FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue();
            }
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager saveParticipantActivities error :",e);
        }
        transaction.commit();
        return message;
    }

    public ApiSimpleResponse getPreferences(Integer userId){
        ApiSimpleResponse response = new ApiSimpleResponse();
        try{
            List<ParticipantStudies> participantStudiesList = getParticipantStudiesList(userId);
            if(null!=participantStudiesList && participantStudiesList.size() >0){
                List<StudiesBean> studiesBeenList = new ArrayList<StudiesBean>();
                for (ParticipantStudies participantStudies : participantStudiesList){
                    if(participantStudies.getAppToken() == null){
                        StudiesBean studiesBean = new StudiesBean();
                        if(participantStudies.getStudyId() != null)
                            studiesBean.setStudyId(String.valueOf(participantStudies.getStudyId()));
                        if(participantStudies.getBookmark() != null)
                            studiesBean.setBookmarked(participantStudies.getBookmark());
                        if(participantStudies.getStatus() != null)
                            studiesBean.setStatus(participantStudies.getStatus());
                        studiesBeenList.add(studiesBean);
                    }
                }
                response.put(FdahpUserRegUtil.ErrorCodes.STUDIES.getValue(),studiesBeenList);
            }
            List<ParticipantActivities> participantActivitiesList = getParticipantActivitiesList(userId);
            if(null!=participantActivitiesList && participantActivitiesList.size() > 0){
                List<ActivitiesBean> activitiesBeanList = new ArrayList<ActivitiesBean>();
                for (ParticipantActivities participantActivities : participantActivitiesList)
                {

                    ActivitiesBean activitiesBean = new ActivitiesBean();
                    if(participantActivities.getStudyId()!=null)
                        activitiesBean.setStudyId(String.valueOf(participantActivities.getStudyId()));
                    if(participantActivities.getActivityId()!=null)
                        activitiesBean.setActivityId(String.valueOf(participantActivities.getActivityId()));
                    if(participantActivities.getStatus() != null)
                        activitiesBean.setStatus(participantActivities.getStatus());
                    if(participantActivities.getActivityVersion()!=null)
                        activitiesBean.setActivityVersion(participantActivities.getActivityVersion());
                    if(participantActivities.getBookmark()!=null)
                        activitiesBean.setBookmarked(participantActivities.getBookmark());
                    activitiesBeanList.add(activitiesBean);
                }
                response.put(FdahpUserRegUtil.ErrorCodes.ACTIVITIES.getValue(),activitiesBeanList);
            }
            response.put(FdahpUserRegUtil.ErrorCodes.MESSAGE.getValue(),FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue().toLowerCase());
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager getPreferences error :",e);
        }
        return response;
    }

    public ParticipantStudies getParticipantStudies(Integer studyId,Integer userId){
        ParticipantStudies participantStudies = null;
        try{
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("ParticipantId"), userId);
            filter.addCondition(FieldKey.fromParts("StudyId"), studyId);
            participantStudies =  new TableSelector(FdahpUserRegWSSchema.getInstance().getParticipantStudies(),filter,null).getObject(ParticipantStudies.class);
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager getParticipantStudies()",e);
        }
        return  participantStudies;
    }

    public List<ParticipantActivities> getParticipantActivitiesList(Integer studyId,Integer userId){
        List<ParticipantActivities> participantActivitiesList = null;
        try{
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("ParticipantId"), userId);
            filter.addCondition(FieldKey.fromParts("StudyId"), studyId);
            participantActivitiesList = new TableSelector(FdahpUserRegWSSchema.getInstance().getParticipantActivities(),filter,null).getArrayList(ParticipantActivities.class);
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager getParticipantStudies()",e);
        }
        return participantActivitiesList;
    }

    public String withDrawStudy(Integer studyId,Integer userId){
        String message = FdahpUserRegUtil.ErrorCodes.FAILURE.getValue();
        try{
            TableInfo table = FdahpUserRegWSSchema.getInstance().getParticipantStudies();
            table.setAuditBehavior(AuditBehaviorType.DETAILED);
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("ParticipantId"), userId);
            filter.addCondition(FieldKey.fromParts("StudyId"), studyId);
            int count = Table.delete(table,filter);
            if(count >0)
                message = FdahpUserRegUtil.ErrorCodes.SUCCESS.getValue();
        }catch (Exception e){
            _log.error("HealthStudiesGatewayManager withDrawStudy()",e);
        }
        return message;
    }

    public AuthInfo getAuthInfo(Container container,String authKey,Integer participantId){
        AuthInfo authInfo = null;
        try{
            SimpleFilter filter = new SimpleFilter();
            filter.addCondition(FieldKey.fromParts("AuthKey"), authKey);
            filter.addCondition(FieldKey.fromParts("ParticipantId"), participantId);
            authInfo  = new TableSelector(FdahpUserRegWSSchema.getInstance().getAuthInfo(),filter,null).getObject(AuthInfo.class);
        }catch (Exception e){
            _log.error("FdahpUserRegWSManger getAuthInfo ()",e);
        }
        return authInfo;
    }
    public AuthInfo updateAuthInfo(AuthInfo authInfo){
        DbScope dbScope = FdahpUserRegWSSchema.getInstance().getSchema().getScope();
        DbScope.Transaction transaction = dbScope.ensureTransaction();
        try{
            TableInfo table = FdahpUserRegWSSchema.getInstance().getAuthInfo();
            table.setAuditBehavior(AuditBehaviorType.DETAILED);
            if(null !=authInfo){
                authInfo.setDeviceToken(authInfo.getDeviceToken());
                authInfo.setDeviceType(authInfo.getDeviceType());
                authInfo.setModifiedOn(new Date());
                Table.update(null,table, authInfo,authInfo.getAuthId());
            }

        }catch (Exception e){
            _log.error("updateAuthInfo:",e);
        }
        transaction.commit();
        return authInfo;
    }

}



