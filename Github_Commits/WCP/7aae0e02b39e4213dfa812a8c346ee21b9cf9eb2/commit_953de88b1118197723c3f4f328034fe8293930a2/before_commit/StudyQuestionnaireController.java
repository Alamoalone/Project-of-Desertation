package com.fdahpstudydesigner.controller;

import com.fdahpstudydesigner.bean.*;
import com.fdahpstudydesigner.bo.*;
import com.fdahpstudydesigner.service.StudyActiveTasksService;
import com.fdahpstudydesigner.service.StudyQuestionnaireService;
import com.fdahpstudydesigner.service.StudyService;
import com.fdahpstudydesigner.util.FdahpStudyDesignerConstants;
import com.fdahpstudydesigner.util.FdahpStudyDesignerUtil;
import com.fdahpstudydesigner.util.SessionObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

/** @author BTC */
@Controller
public class StudyQuestionnaireController {
  private static Logger logger = LogManager.getLogger(StudyQuestionnaireController.class.getName());

  @Autowired private StudyActiveTasksService studyActiveTasksService;

  @Autowired private StudyQuestionnaireService studyQuestionnaireService;

  @Autowired private StudyService studyService;

  /**
   * Admin want copy the already existed question into the same study admin has to click the copy
   * icon in the questionnaire list.It will copy the existed questionnaire into the study with out
   * questionnaire short title because the short title will be unique across the study
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/copyQuestionnaire.do")
  public ModelAndView copyStudyQuestionnaire(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - saveOrUpdateFormQuestion - Starts");
    ModelAndView mav = new ModelAndView("instructionsStepPage");
    ModelMap map = new ModelMap();
    QuestionnaireBo copyQuestionnaireBo = null;
    String customStudyId = "";
    String studyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");

        if (StringUtils.isNotEmpty(questionnaireId) && StringUtils.isNotEmpty(customStudyId)) {
          copyQuestionnaireBo =
              studyQuestionnaireService.copyStudyQuestionnaireBo(
                  Integer.valueOf(questionnaireId), customStudyId, sesObj);
        }
        if (copyQuestionnaireBo != null) {
          request.getSession().setAttribute(sessionStudyCount + "actionType", "edit");
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                  "Questionnaire copied successfully.");
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + "questionnaireId",
                  String.valueOf(copyQuestionnaireBo.getId()));
          map.addAttribute("_S", sessionStudyCount);
          if (StringUtils.isNotEmpty(studyId)) {
            studyService.markAsCompleted(
                Integer.valueOf(studyId),
                FdahpStudyDesignerConstants.QUESTIONNAIRE,
                false,
                sesObj,
                customStudyId);
          }
          mav = new ModelAndView("redirect:/adminStudies/viewQuestionnaire.do", map);
        } else {
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG,
                  "Questionnaire not copyied successfully.");
          map.addAttribute("_S", sessionStudyCount);
          mav = new ModelAndView("redirect:/adminStudies/viewStudyQuestionnaires.do", map);
        }
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveOrUpdateFormQuestion - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveOrUpdateFormQuestion - Ends");
    return mav;
  }

  /**
   * Form step contains group of questions.Admin can delete the questions using this method and we
   * will return the list of remaining question to refresh the list
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String : Success/Failure
   */
  @RequestMapping(value = "/adminStudies/deleteFormQuestion.do", method = RequestMethod.POST)
  public void deleteFormQuestionInfo(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - deleteFormQuestionInfo - Starts");
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    String message = FdahpStudyDesignerConstants.FAILURE;
    QuestionnairesStepsBo questionnairesStepsBo = null;
    Map<Integer, QuestionnaireStepBean> qTreeMap = new TreeMap<>();
    ObjectMapper mapper = new ObjectMapper();
    JSONObject questionnaireJsonObject = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String formId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("formId"))
                ? ""
                : request.getParameter("formId");
        String questionId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionId"))
                ? ""
                : request.getParameter("questionId");
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        String questionnairesId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnairesId"))
                ? ""
                : request.getParameter("questionnairesId");
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        if (!formId.isEmpty() && !questionId.isEmpty()) {
          message =
              studyQuestionnaireService.deleteFromStepQuestion(
                  Integer.valueOf(formId), Integer.valueOf(questionId), sesObj, customStudyId);
          if (message.equalsIgnoreCase(FdahpStudyDesignerConstants.SUCCESS)) {
            questionnairesStepsBo =
                studyQuestionnaireService.getQuestionnaireStep(
                    Integer.valueOf(formId),
                    FdahpStudyDesignerConstants.FORM_STEP,
                    null,
                    customStudyId,
                    Integer.valueOf(questionnairesId));
            if (questionnairesStepsBo != null) {
              questionnairesStepsBo.setType(FdahpStudyDesignerConstants.ACTION_TYPE_SAVE);
              studyQuestionnaireService.saveOrUpdateFromStepQuestionnaire(
                  questionnairesStepsBo, sesObj, customStudyId, studyId);
              qTreeMap = questionnairesStepsBo.getFormQuestionMap();

              Integer responseType = null;
              SortedMap<Integer, QuestionnaireStepBean> queMap = questionnairesStepsBo.getFormQuestionMap();
              if (queMap != null && !queMap.isEmpty()) {
                int lastKey = queMap.lastKey();
                responseType = queMap.get(lastKey).getResponseType();
              }
              jsonobject.put("lastResponseType", responseType != null ? String.valueOf(responseType) : responseType);
              questionnaireJsonObject = new JSONObject(mapper.writeValueAsString(qTreeMap));
              jsonobject.put("questionnaireJsonObject", questionnaireJsonObject);
              if (qTreeMap != null) {
                boolean isDone = true;
                if (!qTreeMap.isEmpty()) {
                  for (Entry<Integer, QuestionnaireStepBean> entryKey : qTreeMap.entrySet()) {
                    if (!entryKey.getValue().getStatus()) {
                      isDone = false;
                      break;
                    }
                  }
                }
                jsonobject.put("isDone", isDone);
              }
            }
            if (StringUtils.isNotEmpty(studyId)) {
              studyService.markAsCompleted(
                  Integer.valueOf(studyId),
                  FdahpStudyDesignerConstants.QUESTIONNAIRE,
                  false,
                  sesObj,
                  customStudyId);
            }
          }
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - deleteFormQuestionInfo - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - deleteFormQuestionInfo - Ends");
  }

  /**
   * Deleting of an Questionnaire in Study
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/deleteQuestionnaire.do", method = RequestMethod.POST)
  public void deleteQuestionnaireInfo(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - deleteQuestionnaireInfo - Starts");
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    String message = FdahpStudyDesignerConstants.SUCCESS;
    List<QuestionnaireBo> questionnaires = null;
    JSONArray questionnaireJsonArray = null;
    ObjectMapper mapper = new ObjectMapper();
    String customStudyId = "";
    String actMsg = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        String studyId =
            FdahpStudyDesignerUtil.isEmpty(
                        request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                    == true
                ? ""
                : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        if (!studyId.isEmpty() && !questionnaireId.isEmpty()) {
          message =
              studyQuestionnaireService.deletQuestionnaire(
                  Integer.valueOf(studyId),
                  Integer.valueOf(questionnaireId),
                  sesObj,
                  customStudyId);
          questionnaires =
              studyQuestionnaireService.getStudyQuestionnairesByStudyId(studyId, false);
          if (questionnaires != null && !questionnaires.isEmpty()) {
            questionnaireJsonArray = new JSONArray(mapper.writeValueAsString(questionnaires));
            jsonobject.put(FdahpStudyDesignerConstants.QUESTIONNAIRE_LIST, questionnaireJsonArray);
          }
          if (StringUtils.isNotEmpty(studyId)) {
            studyService.markAsCompleted(
                Integer.valueOf(studyId),
                FdahpStudyDesignerConstants.QUESTIONNAIRE,
                false,
                sesObj,
                customStudyId);
          }
          boolean markAsComplete = true;
          actMsg =
              studyService.validateActivityComplete(
                  studyId, FdahpStudyDesignerConstants.ACTIVITY_TYPE_QUESTIONNAIRE);
          if (!actMsg.equalsIgnoreCase(FdahpStudyDesignerConstants.SUCCESS)) markAsComplete = false;
          jsonobject.put("markAsComplete", markAsComplete);
          jsonobject.put(FdahpStudyDesignerConstants.ACTIVITY_MESSAGE, actMsg);
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - deleteQuestionnaireInfo - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - deleteQuestionnaireInfo - Ends");
  }

  /**
   * Delete of an questionnaire step(Instruction,Question,Form) which are listed in questionnaire.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/deleteQuestionnaireStep.do", method = RequestMethod.POST)
  public void deleteQuestionnaireStepInfo(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - deleteQuestionnaireStepInfo - Starts");
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    String message = FdahpStudyDesignerConstants.FAILURE;
    QuestionnaireBo questionnaireBo = null;
    Map<Integer, QuestionnaireStepBean> qTreeMap = new TreeMap<Integer, QuestionnaireStepBean>();
    ObjectMapper mapper = new ObjectMapper();
    JSONObject questionnaireJsonObject = null;
    String customStudyId = "";
    boolean isAnchorQuestionnaire = false;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        String stepId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("stepId"))
                ? ""
                : request.getParameter("stepId");
        String deletionStepId =
                FdahpStudyDesignerUtil.isEmpty(request.getParameter("deletionId"))
                        ? ""
                        : request.getParameter("deletionId");
        String stepType =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("stepType"))
                ? ""
                : request.getParameter("stepType");
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        //getting groupId by sending stepId
        Integer groupId = studyQuestionnaireService.getGroupId(stepId);
        //getting noOfstepsCount by sending groupId
        List<GroupMappingBo> groupMappingBo = studyQuestionnaireService.getStepId(String.valueOf(groupId),questionnaireId);
        //writing conditon for checking the count
        if (!stepId.isEmpty() && !questionnaireId.isEmpty() && !stepType.isEmpty()) {
          if(groupMappingBo.size() > 2 || groupMappingBo.size() == 0){
          String msgg = studyQuestionnaireService.deleteStepBasedOnStepId(stepId);
          message =
              studyQuestionnaireService.deleteQuestionnaireStep(
                  Integer.valueOf(stepId),
                  Integer.valueOf(questionnaireId),
                  stepType,
                  sesObj,
                  customStudyId,
                  Integer.valueOf(deletionStepId)
              );
          if (message.equalsIgnoreCase(FdahpStudyDesignerConstants.SUCCESS)) {
            questionnaireBo =
                studyQuestionnaireService.getQuestionnaireById(
                    Integer.valueOf(questionnaireId), customStudyId);
            if (questionnaireBo != null) {
              questionnaireBo.setStatus(false);
              questionnaireBo.setType(FdahpStudyDesignerConstants.CONTENT);
              studyQuestionnaireService.saveOrUpdateQuestionnaire(
                  questionnaireBo, sesObj, customStudyId, null);
              qTreeMap =
                  studyQuestionnaireService.getQuestionnaireStepList(questionnaireBo.getId());
              questionnaireJsonObject = new JSONObject(mapper.writeValueAsString(qTreeMap));
              jsonobject.put("questionnaireJsonObject", questionnaireJsonObject);
              if (qTreeMap != null) {
                boolean isDone = true;
                for (Entry<Integer, QuestionnaireStepBean> entry : qTreeMap.entrySet()) {
                  QuestionnaireStepBean questionnaireStepBean = entry.getValue();
                  if (questionnaireStepBean.getStatus() != null
                      && !questionnaireStepBean.getStatus()) {
                    isDone = false;
                    break;
                  }
                  if (entry.getValue().getFromMap() != null) {
                    if (!entry.getValue().getFromMap().isEmpty()) {
                      for (Entry<Integer, QuestionnaireStepBean> entryKey :
                          entry.getValue().getFromMap().entrySet()) {
                        if (!entryKey.getValue().getStatus()) {
                          isDone = false;
                          break;
                        }
                      }
                    } else {
                      isDone = false;
                      break;
                    }
                  }
                }
                jsonobject.put("isDone", isDone);
              }
              isAnchorQuestionnaire =
                  studyQuestionnaireService.isAnchorDateExistByQuestionnaire(
                      Integer.valueOf(questionnaireId));
              jsonobject.put("isAnchorQuestionnaire", isAnchorQuestionnaire);
            }
            String studyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
            if (StringUtils.isNotEmpty(studyId)) {
              studyService.markAsCompleted(
                  Integer.valueOf(studyId),
                  FdahpStudyDesignerConstants.QUESTIONNAIRE,
                  false,
                  sesObj,
                  customStudyId);
            }
            jsonobject.put("allowReorder", studyQuestionnaireService.isPreloadLogicAndPipingEnabled(
                    questionnaireBo != null ? questionnaireBo.getId() : null));
          }
        }else{
            message = FdahpStudyDesignerConstants.DELETE_ASSIGNSTEP_FAILURE;
        }
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - deleteQuestionnaireStepInfo - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - deleteQuestionnaireStepInfo - Ends");
  }

  /**
   * A questionnaire contains the form step.form step carries multiple questions.Here we described
   * to load the form step of an questionnaire
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/formStep.do")
  public ModelAndView getFormStepPage(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - getFormStepPage - Starts");
    ModelAndView mav = new ModelAndView("formStepPage");
    String sucMsg = "";
    String errMsg = "";
    ModelMap map = new ModelMap();
    List<GroupsBo> groupsListPreLoad = null;
    List<GroupsBo> groupsListPostLoad = null;
    GroupsBo groupsBo = null;
    QuestionnaireBo questionnaireBo = null;
    QuestionnairesStepsBo questionnairesStepsBo = null;
    List<GroupMappingBo> groupStepLists = new ArrayList<>();
    StudyBo studyBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        request.getSession().removeAttribute(sessionStudyCount + "questionId");
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG)) {
          sucMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, sucMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
        }
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG)) {
          errMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.ERR_MSG, errMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
        }
        request.getSession().removeAttribute(sessionStudyCount + "actionTypeForFormStep");
        String actionType =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionType"))
                ? ""
                : request.getParameter("actionType");
        if (StringUtils.isEmpty(actionType)) {
          actionType = (String) request.getSession().getAttribute(sessionStudyCount + "actionType");
        }

        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);

        String actionTypeForQuestionPage =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionTypeForQuestionPage"))
                ? ""
                : request.getParameter("actionTypeForQuestionPage");
        if (StringUtils.isEmpty(actionTypeForQuestionPage)) {
          actionTypeForQuestionPage =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + "actionTypeForQuestionPage");
          if ("edit".equals(actionTypeForQuestionPage)) {
            map.addAttribute("actionTypeForQuestionPage", "edit");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "edit");
          } else if ("view".equals(actionTypeForQuestionPage)) {
            map.addAttribute("actionTypeForQuestionPage", "view");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "view");
          } else {
            map.addAttribute("actionTypeForQuestionPage", "add");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "add");
          }
        } else {
          map.addAttribute("actionTypeForQuestionPage", actionTypeForQuestionPage);
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + "actionTypeForQuestionPage", actionTypeForQuestionPage);
        }

        String formId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("formId"))
                ? ""
                : request.getParameter("formId");
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        String isLive =
                (String)
                        request
                                .getSession()
                                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.IS_LIVE);
        if (StringUtils.isEmpty(studyId)) {
          studyId =
              FdahpStudyDesignerUtil.isEmpty(
                          request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                      == true
                  ? ""
                  : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
          request
              .getSession()
              .setAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID, studyId);
        }
        if (StringUtils.isNotEmpty(studyId)) {
          studyBo = studyService.getStudyById(studyId, sesObj.getUserId());
          map.addAttribute(FdahpStudyDesignerConstants.STUDY_BO, studyBo);
          String languages = studyBo.getSelectedLanguages();
          List<String> langList;
          Map<String, String> langMap = new HashMap<>();
          if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
            langList = Arrays.asList(languages.split(","));
            for (String string : langList) {
              langMap.put(string, MultiLanguageCodes.getValue(string));
            }
          }
          map.addAttribute("languageList", langMap);
        }
        String language = request.getParameter("language");
        map.addAttribute("currLanguage", language);
        map.addAttribute("isAutoSaved", request.getParameter("isAutoSaved"));
        if (StringUtils.isEmpty(formId)) {
          formId = (String) request.getSession().getAttribute(sessionStudyCount + "formId");
          request.getSession().setAttribute(sessionStudyCount + "formId", formId);
        }
        if (StringUtils.isEmpty(questionnaireId)) {
          questionnaireId =
              (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
        }
        if (StringUtils.isNotEmpty(questionnaireId) && null != studyBo) {
          request.getSession().removeAttribute(sessionStudyCount + "actionType");
          questionnaireBo =
              studyQuestionnaireService.getQuestionnaireById(
                  Integer.valueOf(questionnaireId), studyBo.getCustomStudyId());
          map.addAttribute("questionnaireBo", questionnaireBo);
          if ("edit".equals(actionType)) {
            map.addAttribute("actionType", "edit");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "edit");
          } else if ("view".equals(actionType)) {
            map.addAttribute("actionType", "view");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "view");
          } else {
            map.addAttribute("actionType", "add");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "add");
          }
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
        }
        if (formId != null && !formId.isEmpty() && null != studyBo) {
          questionnairesStepsBo =
              studyQuestionnaireService.getQuestionnaireStep(
                  Integer.valueOf(formId),
                  FdahpStudyDesignerConstants.FORM_STEP,
                  questionnaireBo.getShortTitle(),
                  studyBo.getCustomStudyId(),
                  questionnaireBo.getId());
          if (questionnairesStepsBo != null) {
            List<QuestionnairesStepsBo> destinationStepList = studyQuestionnaireService
                    .getPostLoadDestinationKeys(StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : null,
                            questionnairesStepsBo.getSequenceNo(),
                            questionnairesStepsBo.getStepId());
            map.addAttribute("destinationStepList", destinationStepList);
            if (!questionnairesStepsBo.getStatus() && StringUtils.isNotEmpty(studyId)) {
              studyService.markAsCompleted(
                  Integer.parseInt(studyId),
                  FdahpStudyDesignerConstants.QUESTIONNAIRE,
                  false,
                  sesObj,
                  customStudyId);
            }
          }

          Integer responseType = null;
          int queId = 0;
          if (questionnairesStepsBo != null) {
            SortedMap<Integer, QuestionnaireStepBean> queMap = questionnairesStepsBo.getFormQuestionMap();
            if (queMap != null && !queMap.isEmpty()) {
              int lastKey = queMap.lastKey();
              responseType = queMap.get(lastKey).getResponseType();
              queId = queMap.get(lastKey).getQuestionInstructionId();
            }
          }
          map.put("lastResponseType", responseType);
          boolean isMultiSelect = false;
          if (responseType != null && queId != 0 && responseType.equals(6)) {
            isMultiSelect = studyQuestionnaireService.isQuestionMultiSelect(queId);
          }
          map.addAttribute("operators", isMultiSelect ? null : this.getOperatorsListByResponseType(responseType));
          int preloadQueId = StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : 0;
          int preloadSeqNo = questionnairesStepsBo != null ? questionnairesStepsBo.getSequenceNo() : 0;
          Integer currStepId = questionnairesStepsBo != null ? questionnairesStepsBo.getStepId() : null;
          groupsBo = studyQuestionnaireService.getGroupIdBySendingQuestionStepId(currStepId);
          map.addAttribute("groupsBo", groupsBo);
          if(groupsBo != null) {
            Integer id = groupsBo.getId();
            // Skippable should be false and non-editable for last step in a group if group level pre-load logic is enabled
            //getting Assigned stepList for particular groupId by sending questionStepId
            if (id != null) {
              groupStepLists = studyQuestionnaireService.getStepId(String.valueOf(id), questionnaireId);
            }
          }
          GroupMappingBo lastStepObject;
          boolean IsSkippableFlag = false;
          if(groupStepLists.size() != 0) {
            lastStepObject = groupStepLists.get(groupStepLists.size() - 1);
            if(lastStepObject.getQuestionnaireStepId().equals(currStepId)){
              IsSkippableFlag = true;
            }
          }
          map.addAttribute("IsSkippableFlag", IsSkippableFlag);
          if (questionnairesStepsBo != null) {
            if (questionnairesStepsBo.getDifferentSurveyPreLoad() != null && questionnairesStepsBo.getDifferentSurveyPreLoad()) {
              preloadQueId = questionnairesStepsBo.getPreLoadSurveyId();
              preloadSeqNo = -1;
            }
            map.addAttribute("sameSurveyPreloadSourceKeys", studyQuestionnaireService
                    .getSameSurveySourceKeys(preloadQueId, preloadSeqNo, "preload", currStepId,
                            StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : null));
          }

          Integer stepId = questionnairesStepsBo != null ? questionnairesStepsBo.getInstructionFormId() : null;
          String queIdForGroups = questionnaireId;
          if (questionnairesStepsBo != null && questionnairesStepsBo.getDifferentSurveyPreLoad() != null
                  && questionnairesStepsBo.getDifferentSurveyPreLoad()
                  && questionnairesStepsBo.getPreLoadSurveyId() != null) {
            queIdForGroups = String.valueOf(questionnairesStepsBo.getPreLoadSurveyId());
          }
          if (StringUtils.isNotEmpty(questionnaireId)) {
            groupsListPreLoad = studyQuestionnaireService.getGroupsForPreloadAndPostLoad(questionnaireId, queIdForGroups, stepId, true);
            groupsListPostLoad = studyQuestionnaireService.getGroupsForPreloadAndPostLoad(questionnaireId, null, stepId, false);
          }
          map.addAttribute("groupsListPostLoad", groupsListPostLoad);
          map.addAttribute("groupsListPreLoad", groupsListPreLoad);

          if (FdahpStudyDesignerConstants.YES.equals(isLive)) {
            studyId = studyBo.getCustomStudyId();
          }
          map.addAttribute("questionnaireIds", studyQuestionnaireService.getQuestionnairesForPiping(questionnaireId, studyId, FdahpStudyDesignerConstants.YES.equals(isLive)));
          map.addAttribute("questionnairesStepsBo", questionnairesStepsBo);
          request.getSession().setAttribute(sessionStudyCount + "formId", formId);
          if (FdahpStudyDesignerUtil.isNotEmpty(language)
              && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
            FormLangBO formLangBO =
                studyQuestionnaireService.getFormLangBO(Integer.parseInt(formId), language);
            map.addAttribute("formLangBO", formLangBO);
            List<QuestionLangBO> questionLangBOList =
                studyQuestionnaireService.syncAndGetQuestionLangByFormId(
                    questionnairesStepsBo,
                    Integer.parseInt(questionnaireId),
                    Integer.parseInt(formId),
                    language);
            map.addAttribute("questionLangBOList", questionLangBOList);
            this.setStudyLangData(studyId, language, map);
          }
        }

        map.addAttribute(
            FdahpStudyDesignerConstants.QUESTION_STEP,
            request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.QUESTION_STEP));
        map.addAttribute("questionnaireId", questionnaireId);
        request
            .getSession()
            .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.QUESTION_STEP);
        map.addAttribute("_S", sessionStudyCount);
        mav = new ModelAndView("formStepPage", map);
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - getFormStepPage - Error", e);
    }
    logger.info("StudyQuestionnaireController - getFormStepPage - Ends");
    return mav;
  }

  private List<String> getOperatorsListByResponseType(Integer responseType) {
    List<String> operatorsList = new ArrayList<>();
    try {
      if (responseType != null) {
        if (responseType.equals(1) || responseType.equals(2) ||
                responseType.equals(8)) {
          operatorsList = Arrays.asList("<", ">", "=", "!=", "<=", ">=");
        } else if ((responseType >= 3 && responseType <= 7) || responseType == 11) {
          operatorsList = Arrays.asList("=", "!=");
        } else if (responseType.equals(14)) {
          operatorsList = Arrays.asList("<", ">");
        }
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - getOperatorsListByResponseType - Error", e);
    }
    return operatorsList;
  }

  /**
   * Load the Question page of form step inside questionnaire.Question contains the question level
   * attributes and response level attributes
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/formQuestion.do")
  public ModelAndView getFormStepQuestionPage(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - getFormStepQuestionPage - Starts");
    ModelAndView mav = new ModelAndView("questionPage");
    String sucMsg = "";
    String errMsg = "";
    ModelMap map = new ModelMap();
    QuestionnaireBo questionnaireBo = null;
    QuestionnairesStepsBo questionnairesStepsBo = null;
    StudyBo studyBo = null;
    QuestionsBo questionsBo = null;
    List<GroupsBo> groupsList = null;
    List<String> timeRangeList = new ArrayList<String>();
    List<StatisticImageListBo> statisticImageList = new ArrayList<>();
    List<ActivetaskFormulaBo> activetaskFormulaList = new ArrayList<>();
    List<QuestionResponseTypeMasterInfoBo> questionResponseTypeMasterInfoList = new ArrayList<>();
    List<HealthKitKeysInfo> healthKitKeysInfo = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG)) {
          sucMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, sucMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
        }
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG)) {
          errMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.ERR_MSG, errMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
        }

        request.getSession().removeAttribute(sessionStudyCount + "actionTypeForFormStep");
        String actionTypeForQuestionPage =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionTypeForQuestionPage"))
                ? ""
                : request.getParameter("actionTypeForQuestionPage");
        if (StringUtils.isEmpty(actionTypeForQuestionPage)) {
          actionTypeForQuestionPage =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + "actionTypeForQuestionPage");
        }

        String actionTypeForFormStep =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionTypeForFormStep"))
                ? ""
                : request.getParameter("actionTypeForFormStep");
        map.addAttribute("nav", request.getParameter("nav"));
        if (StringUtils.isEmpty(actionTypeForFormStep)) {
          actionTypeForFormStep =
              (String)
                  request.getSession().getAttribute(sessionStudyCount + "actionTypeForFormStep");
          if ("edit".equals(actionTypeForFormStep)) {
            map.addAttribute("actionTypeForFormStep", "edit");
            request.getSession().setAttribute(sessionStudyCount + "actionTypeForFormStep", "edit");
          } else if ("view".equals(actionTypeForFormStep)) {
            map.addAttribute("actionTypeForFormStep", "view");
            request.getSession().setAttribute(sessionStudyCount + "actionTypeForFormStep", "view");
          } else {
            map.addAttribute("actionTypeForFormStep", "add");
            request.getSession().setAttribute(sessionStudyCount + "actionTypeForFormStep", "add");
          }
        } else {
          map.addAttribute("actionTypeForFormStep", actionTypeForFormStep);
        }

        String formId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("formId"))
                ? ""
                : request.getParameter("formId");
        String questionId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionId"))
                ? ""
                : request.getParameter("questionId");
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        if (StringUtils.isEmpty(studyId)) {
          studyId =
              FdahpStudyDesignerUtil.isEmpty(
                      request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                  ? ""
                  : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
          request.getSession().setAttribute(FdahpStudyDesignerConstants.STUDY_ID, studyId);
        }
        String language = request.getParameter("language");
        map.addAttribute("currLanguage", language);
        map.addAttribute("isAutoSaved", request.getParameter("isAutoSaved"));
        if (StringUtils.isNotEmpty(studyId)) {
          studyBo = studyService.getStudyById(studyId, sesObj.getUserId());
          boolean isExists =
              studyQuestionnaireService.isAnchorDateExistsForStudy(
                  Integer.valueOf(studyId), studyBo.getCustomStudyId());
          map.addAttribute("isAnchorDate", isExists);
          map.addAttribute(FdahpStudyDesignerConstants.STUDY_BO, studyBo);
        }

        if (StringUtils.isEmpty(formId)) {
          formId = (String) request.getSession().getAttribute(sessionStudyCount + "formId");
          request.getSession().setAttribute(sessionStudyCount + "formId", formId);
        }
        if (StringUtils.isEmpty(questionId)) {
          questionId = (String) request.getSession().getAttribute(sessionStudyCount + "questionId");
          request.getSession().setAttribute(sessionStudyCount + "questionId", questionId);
        }
        if (StringUtils.isEmpty(questionnaireId)) {
          questionnaireId =
              (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
        }
        if (StringUtils.isNotEmpty(questionnaireId)) {
          request.getSession().removeAttribute(sessionStudyCount + "actionTypeForQuestionPage");
          questionnaireBo =
              studyQuestionnaireService.getQuestionnaireById(
                  Integer.valueOf(questionnaireId), studyBo.getCustomStudyId());
          map.addAttribute("questionnaireBo", questionnaireBo);
          if ("edit".equals(actionTypeForQuestionPage)) {
            map.addAttribute("actionTypeForQuestionPage", "edit");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "edit");
          } else if ("view".equals(actionTypeForQuestionPage)) {
            map.addAttribute("actionTypeForQuestionPage", "view");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "view");
          } else {
            map.addAttribute("actionTypeForQuestionPage", "add");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "add");
          }
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
          if (questionnaireBo != null && StringUtils.isNotEmpty(questionnaireBo.getFrequency())) {
            String frequency = questionnaireBo.getFrequency();
            if (questionnaireBo
                .getFrequency()
                .equalsIgnoreCase(FdahpStudyDesignerConstants.FREQUENCY_TYPE_DAILY)) {
              if (questionnaireBo.getQuestionnairesFrequenciesList() != null
                  && questionnaireBo.getQuestionnairesFrequenciesList().size() > 1) {
                frequency = questionnaireBo.getFrequency();
              } else {
                frequency = FdahpStudyDesignerConstants.FREQUENCY_TYPE_ONE_TIME;
              }
            }
            timeRangeList = FdahpStudyDesignerUtil.getTimeRangeList(frequency);
          }
        }
        if (formId != null && !formId.isEmpty()) {
          questionnairesStepsBo =
              studyQuestionnaireService.getQuestionnaireStep(
                  Integer.valueOf(formId),
                  FdahpStudyDesignerConstants.FORM_STEP,
                  questionnaireBo.getShortTitle(),
                  studyBo.getCustomStudyId(),
                  questionnaireBo.getId());

          if(questionId != null && !questionId.isEmpty()) {
            List<FormMappingBo> questionLists = new ArrayList<>();
            GroupsBo groupsBo = null;
            Integer currStepId = questionnairesStepsBo != null ? questionnairesStepsBo.getStepId() : null;
            groupsBo = studyQuestionnaireService.getGroupIdBySendingQuestionStepId(currStepId);
            map.addAttribute("groupsBo", groupsBo);
            //getting List of questions for this particular formId
            questionLists = studyQuestionnaireService.getListOfQuestions(Integer.valueOf(formId));
            FormMappingBo lastQuestionStepObject;
            boolean IsQuestionSkippableFlag = false;
            if (questionLists.size() != 0) {
              lastQuestionStepObject = questionLists.get(questionLists.toArray().length - 1);
              System.out.println(lastQuestionStepObject.getQuestionId());
              System.out.println(questionId);
              if (lastQuestionStepObject.getQuestionId().equals(Integer.valueOf(questionId))) {
                IsQuestionSkippableFlag = true;
              }
            }
            map.addAttribute("IsQuestionSkippableFlag", IsQuestionSkippableFlag);
          }
          if (questionId != null && !questionId.isEmpty()) {
            questionsBo =
                studyQuestionnaireService.getQuestionsById(
                    Integer.valueOf(questionId),
                    questionnaireBo.getShortTitle(),
                    studyBo.getCustomStudyId());
            map.addAttribute("questionsBo", questionsBo);
            request.getSession().setAttribute(sessionStudyCount + "questionId", questionId);
            if (questionnairesStepsBo != null) {
              List<QuestionnairesStepsBo> destinationStepList = studyQuestionnaireService
                      .getPostLoadDestinationKeys(StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : null,
                              questionnairesStepsBo.getSequenceNo(),
                              questionnairesStepsBo.getStepId());
              map.addAttribute("destinationStepList", destinationStepList);
            }

            QuestionLangBO spanishQuestionBo = studyQuestionnaireService.getQuestionLangBO(Integer.parseInt(questionId), "es");
            if (spanishQuestionBo != null) {
              String[] displayText = StringUtils.isNotBlank(spanishQuestionBo.getDisplayText())
                      ? spanishQuestionBo.getDisplayText().split("\\|")
                      : null;
              String[] description = StringUtils.isNotBlank(spanishQuestionBo.getTextChoiceDescription())
                      ? spanishQuestionBo.getTextChoiceDescription().split("\\|")
                      : null;
              int i = 0;
              if (questionsBo != null && questionsBo.getQuestionResponseSubTypeList() != null) {
                for (QuestionResponseSubTypeBo subTypeBo : questionsBo.getQuestionResponseSubTypeList()) {
                  if (subTypeBo != null) {
                    subTypeBo.setDisplayTextLang(displayText != null
                            ? (displayText.length > i ? displayText[i] : "")
                            : null);
                    subTypeBo.setDescriptionLang(description != null
                            ? (description.length > i ? description[i] : "")
                            : null);
                  }
                  i++;
                }
              }
            }

            if (FdahpStudyDesignerUtil.isNotEmpty(language)
                    && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
              QuestionLangBO questionLangBO =
                      studyQuestionnaireService.getQuestionLangBO(
                              Integer.parseInt(questionId), language);
              map.addAttribute("questionLangBO", questionLangBO);
              this.setStudyLangData(studyId, language, map);
            }
          }
          String languages = studyBo.getSelectedLanguages();
          List<String> langList = new ArrayList<>();
          Map<String, String> langMap = new HashMap<>();
          if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
            langList = Arrays.asList(languages.split(","));
            for (String string : langList) {
              langMap.put(string, MultiLanguageCodes.getValue(string));
            }
          }
          map.addAttribute("languageList", langMap);
          map.addAttribute("formId", formId);
          map.addAttribute("questionnairesStepsBo", questionnairesStepsBo);
          request.getSession().setAttribute(sessionStudyCount + "formId", formId);
        }
        statisticImageList = studyActiveTasksService.getStatisticImages();
        activetaskFormulaList = studyActiveTasksService.getActivetaskFormulas();
        questionResponseTypeMasterInfoList = studyQuestionnaireService.getQuestionReponseTypeList();
        if (studyBo != null) {
          if (studyBo.getPlatform().contains(FdahpStudyDesignerConstants.IOS)) {
            healthKitKeysInfo = studyQuestionnaireService.getHeanlthKitKeyInfoList();
            map.addAttribute("healthKitKeysInfo", healthKitKeysInfo);
          }
        }
        boolean isLastQuestion = false;
        if (questionnairesStepsBo != null &&
                questionnairesStepsBo.getDefaultVisibility() != null &&
                !questionnairesStepsBo.getDefaultVisibility()) {
          if (StringUtils.isNotBlank(questionId)) {
            if (Arrays.asList(1, 2, 3, 7, 8, 11, 14).contains(questionsBo.getResponseType())) {
              isLastQuestion = studyQuestionnaireService.isLastFormQuestion(formId, questionId);
            }
          } else {
            isLastQuestion = true;
          }
        }
        map.addAttribute("isLastFormQuestion", isLastQuestion);
        map.addAttribute("timeRangeList", timeRangeList);
        map.addAttribute("statisticImageList", statisticImageList);
        map.addAttribute("activetaskFormulaList", activetaskFormulaList);
        map.addAttribute("questionResponseTypeMasterInfoList", questionResponseTypeMasterInfoList);
        request
            .getSession()
            .setAttribute(sessionStudyCount + FdahpStudyDesignerConstants.QUESTION_STEP, "Yes");
        map.addAttribute("_S", sessionStudyCount);
        mav = new ModelAndView("questionPage", map);
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - getFormStepQuestionPage - Error", e);
    }
    logger.info("StudyQuestionnaireController - getFormStepQuestionPage - Ends");
    return mav;
  }

  /**
   * Instruction Step Page in Questionnaire.Lays down instructions for the user in mobile app.Which
   * contains the short title instruction title and text
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/instructionsStep.do")
  public ModelAndView getInstructionsPage(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - getInstructionsPage - Starts");
    ModelAndView mav = new ModelAndView("instructionsStepPage");
    String sucMsg = "";
    String errMsg = "";
    ModelMap map = new ModelMap();
    InstructionsBo instructionsBo = null;
    QuestionnaireBo questionnaireBo = null;
    QuestionnairesStepsBo questionnairesStepsBo = null;
    List<GroupsBo> groupsPostLoadList = null;
    StudyBo studyBo = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG)) {
          sucMsg = (String) request.getSession().getAttribute(FdahpStudyDesignerConstants.SUC_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, sucMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
        }
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG)) {
          errMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.ERR_MSG, errMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
        }
        String instructionId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("instructionId")) == true
                ? ""
                : request.getParameter("instructionId");
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId")) == true
                ? ""
                : request.getParameter("questionnaireId");
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);

        request.getSession().removeAttribute(sessionStudyCount + "actionTypeForQuestionPage");
        String actionType =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionType"))
                ? ""
                : request.getParameter("actionType");
        if (StringUtils.isEmpty(actionType)) {
          actionType = (String) request.getSession().getAttribute(sessionStudyCount + "actionType");
        }
        String isLive =
                (String)
                        request
                                .getSession()
                                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.IS_LIVE);
        String actionTypeForQuestionPage =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionTypeForQuestionPage"))
                ? ""
                : request.getParameter("actionTypeForQuestionPage");
        if (StringUtils.isEmpty(actionTypeForQuestionPage)) {
          actionTypeForQuestionPage =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + "actionTypeForQuestionPage");
          if ("edit".equals(actionTypeForQuestionPage)) {
            map.addAttribute("actionTypeForQuestionPage", "edit");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "edit");
          } else if ("view".equals(actionTypeForQuestionPage)) {
            map.addAttribute("actionTypeForQuestionPage", "view");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "view");
          } else {
            map.addAttribute("actionTypeForQuestionPage", "add");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "add");
          }
        } else {
          map.addAttribute("actionTypeForQuestionPage", actionTypeForQuestionPage);
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + "actionTypeForQuestionPage", actionTypeForQuestionPage);
        }
        if (StringUtils.isEmpty(studyId)) {
          studyId =
              FdahpStudyDesignerUtil.isEmpty(
                          request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                      == true
                  ? ""
                  : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
          request
              .getSession()
              .setAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID, studyId);
        }
        if (StringUtils.isNotEmpty(studyId)) {
          studyBo = studyService.getStudyById(studyId, sesObj.getUserId());
          map.addAttribute(FdahpStudyDesignerConstants.STUDY_BO, studyBo);

          String languages = studyBo.getSelectedLanguages();
          List<String> langList = new ArrayList<>();
          Map<String, String> langMap = new HashMap<>();
          if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
            langList = Arrays.asList(languages.split(","));
            for (String string : langList) {
              langMap.put(string, MultiLanguageCodes.getValue(string));
            }
          }
          map.addAttribute("languageList", langMap);
        }
        String language = request.getParameter("language");
        if (FdahpStudyDesignerUtil.isNotEmpty(language)
            && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
          this.setStudyLangData(studyId, language, map);
        }
        map.addAttribute("currLanguage", language);
        map.addAttribute("isAutoSaved", request.getParameter("isAutoSaved"));
        if (StringUtils.isEmpty(instructionId)) {
          instructionId =
              (String) request.getSession().getAttribute(sessionStudyCount + "instructionId");
          request.getSession().setAttribute(sessionStudyCount + "instructionId", instructionId);
        }
        if (StringUtils.isEmpty(questionnaireId)) {
          questionnaireId =
              (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
        }
        if (StringUtils.isNotEmpty(questionnaireId) && null != studyBo) {
          request.getSession().removeAttribute(sessionStudyCount + "actionType");
          questionnaireBo =
              studyQuestionnaireService.getQuestionnaireById(
                  Integer.valueOf(questionnaireId), studyBo.getCustomStudyId());
          if ("edit".equals(actionType)) {
            map.addAttribute("actionType", "edit");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "edit");
          } else if ("view".equals(actionType)) {
            map.addAttribute("actionType", "view");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "view");
          } else {
            map.addAttribute("actionType", "add");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "add");
          }
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
          map.addAttribute("questionnaireBo", questionnaireBo);
        }
        if (instructionId != null && !instructionId.isEmpty() && null != studyBo) {
          instructionsBo =
              studyQuestionnaireService.getInstructionsBo(
                  Integer.valueOf(instructionId),
                  questionnaireBo.getShortTitle(),
                  studyBo.getCustomStudyId(),
                  questionnaireBo.getId());
          map.addAttribute("instructionsBo", instructionsBo);
          request.getSession().setAttribute(sessionStudyCount + "instructionId", instructionId);

          InstructionsLangBO instructionsLangBO =
              studyQuestionnaireService.getInstructionLangBO(
                  Integer.parseInt(instructionId), language);
          map.addAttribute("instructionsLangBO", instructionsLangBO);

          questionnairesStepsBo =
                  studyQuestionnaireService.getQuestionnaireStep(
                          Integer.parseInt(instructionId),
                          FdahpStudyDesignerConstants.INSTRUCTION_STEP,
                          questionnaireBo.getShortTitle(),
                          studyBo.getCustomStudyId(),
                          questionnaireBo.getId());
        }

        if (questionnairesStepsBo != null) {
          groupsPostLoadList = studyQuestionnaireService.getGroupsForPreloadAndPostLoad(questionnaireId,
                  null, questionnairesStepsBo.getStepId(), false);
          map.addAttribute("groupsPostLoadList", groupsPostLoadList);
        }
        int pipingQueId = StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : 0;
        int pipingSeqNo = questionnairesStepsBo != null ? questionnairesStepsBo.getSequenceNo() : 0;
        Integer currStepId = questionnairesStepsBo != null ? questionnairesStepsBo.getStepId() : null;
        if (questionnairesStepsBo != null) {
          if (questionnairesStepsBo.getDifferentSurvey() != null && questionnairesStepsBo.getDifferentSurvey()) {
            pipingQueId = questionnairesStepsBo.getPipingSurveyId();
            pipingSeqNo = -1;
          }
        } else {
          pipingSeqNo = -1;
        }
        map.addAttribute("sameSurveyPipingSourceKeys", studyQuestionnaireService.getSameSurveySourceKeys(pipingQueId, pipingSeqNo, "piping", currStepId, null));

        if (instructionsBo != null && questionnairesStepsBo != null) {
          List<QuestionnairesStepsBo> destinationStepList = studyQuestionnaireService
                  .getPostLoadDestinationKeys(StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : null,
                          questionnairesStepsBo.getSequenceNo(),
                          currStepId);
          map.addAttribute("destinationStepList", destinationStepList);
        }

        if (studyBo != null && FdahpStudyDesignerConstants.YES.equals(isLive)) {
          studyId = studyBo.getCustomStudyId();
        }
        map.addAttribute("questionnaireIds", studyQuestionnaireService.getQuestionnairesForPiping(questionnaireId, studyId, FdahpStudyDesignerConstants.YES.equals(isLive)));
        map.addAttribute("questionnaireId", questionnaireId);
        map.addAttribute("_S", sessionStudyCount);
        mav = new ModelAndView("instructionsStepPage", map);
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - getInstructionsPage - Error", e);
    }
    logger.info("StudyQuestionnaireController - getInstructionsPage - Ends");
    return mav;
  }

  /**
   * Load the Questionnaire page of study with all the steps(instruction,question,form) with
   * schedule information. Each step corresponds to one screen on the mobile app.There can be
   * multiple types of QA in a questionnaire depending on the type of response format selected per
   * QA.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return {@link ModelAndView}
   */
  @RequestMapping(value = "/adminStudies/viewQuestionnaire.do")
  public ModelAndView getQuestionnairePage(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - getQuestionnairePage - Starts");
    ModelAndView mav = new ModelAndView("questionnairePage");
    ModelMap map = new ModelMap();
    String sucMsg = "";
    String errMsg = "";
    StudyBo studyBo = null;
    List<GroupsBo> groupsList = null;
    QuestionnaireBo questionnaireBo = null;
    Map<Integer, QuestionnaireStepBean> qTreeMap = new TreeMap<Integer, QuestionnaireStepBean>();
    String customStudyId = "";
    List<AnchorDateTypeBo> anchorTypeList = new ArrayList<>();
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        request.getSession().removeAttribute(sessionStudyCount + "actionTypeForQuestionPage");
        request
            .getSession()
            .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.INSTRUCTION_ID);
        request
            .getSession()
            .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.FORM_ID);
        request
            .getSession()
            .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.QUESTION_ID);
        request
            .getSession()
            .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.QUESTION_STEP);
        request
                .getSession()
                .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.GROUP);
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG)) {
          sucMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, sucMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
        }
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG)) {
          errMsg = (String) request.getSession().getAttribute(FdahpStudyDesignerConstants.ERR_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.ERR_MSG, errMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
        }
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        String permission =
            (String) request.getSession().getAttribute(sessionStudyCount + "permission");

        String actionType =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionType"))
                ? ""
                : request.getParameter("actionType");
        if (StringUtils.isEmpty(actionType)) {
          actionType = (String) request.getSession().getAttribute(sessionStudyCount + "actionType");
        }
        if (StringUtils.isEmpty(studyId)) {
          studyId =
              FdahpStudyDesignerUtil.isEmpty(
                      request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                  ? ""
                  : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
          request
              .getSession()
              .setAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID, studyId);
        }
        if (StringUtils.isNotEmpty(studyId)) {
          studyBo = studyService.getStudyById(studyId, sesObj.getUserId());
          map.addAttribute(FdahpStudyDesignerConstants.STUDY_BO, studyBo);
          anchorTypeList =
              studyQuestionnaireService.getAnchorTypesByStudyId(studyBo.getCustomStudyId());
          map.addAttribute("anchorTypeList", anchorTypeList);

          String languages = studyBo.getSelectedLanguages();
          List<String> langList = new ArrayList<>();
          Map<String, String> langMap = new HashMap<>();
          if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
            langList = Arrays.asList(languages.split(","));
            for (String string : langList) {
              langMap.put(string, MultiLanguageCodes.getValue(string));
            }
          }
          map.addAttribute("languageList", langMap);
        }
        String language = request.getParameter("language");
        map.addAttribute("currLanguage", language);
        String isAutoSaved = request.getParameter("isAutoSaved");
        map.addAttribute("isAutoSaved", isAutoSaved);
        if (StringUtils.isEmpty(questionnaireId)) {
          questionnaireId =
              (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
        }
        customStudyId =
            (String) request.getSession().getAttribute(FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        if (StringUtils.isNotEmpty(questionnaireId)) {
          groupsList =
                  studyQuestionnaireService.getGroupsByStudyId(studyId, questionnaireId, false, null);
        }
        map.addAttribute("groupsList", groupsList);
        if (null != questionnaireId && !questionnaireId.isEmpty()) {
          questionnaireBo =
              studyQuestionnaireService.getQuestionnaireById(
                  Integer.valueOf(questionnaireId), studyBo.getCustomStudyId());
          if (questionnaireBo != null) {
            map.addAttribute(
                "customCount", questionnaireBo.getQuestionnaireCustomScheduleBo().size());
            map.addAttribute("count", questionnaireBo.getQuestionnairesFrequenciesList().size());
            qTreeMap = studyQuestionnaireService.getQuestionnaireStepList(questionnaireBo.getId());
            if (qTreeMap != null) {
              boolean isDone = true;
              for (Entry<Integer, QuestionnaireStepBean> entry : qTreeMap.entrySet()) {
                QuestionnaireStepBean questionnaireStepBean = entry.getValue();
                if (questionnaireStepBean.getStatus() != null
                    && !questionnaireStepBean.getStatus()) {
                  isDone = false;
                  break;
                }
                if (entry.getValue().getFromMap() != null) {
                  if (!entry.getValue().getFromMap().isEmpty()) {
                    for (Entry<Integer, QuestionnaireStepBean> entryKey :
                        entry.getValue().getFromMap().entrySet()) {
                      if (!entryKey.getValue().getStatus()) {
                        isDone = false;
                        break;
                      }
                    }
                  } else {
                    isDone = false;
                    break;
                  }
                }
              }
              map.addAttribute("isDone", isDone);
              if (!isDone && StringUtils.isNotEmpty(studyId)) {
                studyService.markAsCompleted(
                    Integer.valueOf(studyId),
                    FdahpStudyDesignerConstants.QUESTIONNAIRE,
                    false,
                    sesObj,
                    customStudyId);
              }
            }
            map.addAttribute("allowReorder", studyQuestionnaireService.isPreloadLogicAndPipingEnabled(questionnaireBo.getId()));
          }
          if ("edit".equals(actionType)) {
            map.addAttribute("actionType", "edit");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "edit");
          } else {
            map.addAttribute("actionType", "view");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "view");
          }

          QuestionnaireLangBO questionnaireLangBO = null;
          if (FdahpStudyDesignerUtil.isNotEmpty(language) && !"en".equals(language)) {
            questionnaireLangBO =
                studyQuestionnaireService.getQuestionnaireLangById(
                    Integer.parseInt(questionnaireId), language);
          }
          map.addAttribute(
              "questionnaireLangBo",
              questionnaireLangBO != null ? questionnaireLangBO : new QuestionnaireLangBO());
          map.addAttribute("permission", permission);
          map.addAttribute("qTreeMap", qTreeMap);
          map.addAttribute("questionnaireBo", questionnaireBo);
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);

          if (FdahpStudyDesignerUtil.isNotEmpty(language)
              && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
            this.setStudyLangData(studyId, language, map);
            studyQuestionnaireService.syncAndGetLangData(
                qTreeMap, Integer.parseInt(questionnaireId), language, sesObj.getUserId());
            List<QuestionLangBO> questionLangBOList =
                studyQuestionnaireService.getQuestionLangByQuestionnaireId(
                    Integer.parseInt(questionnaireId), language);
            map.addAttribute("questionLangBOList", questionLangBOList);
            List<FormLangBO> formLangList =
                studyQuestionnaireService.getFormLangByQuestionnaireId(
                    Integer.parseInt(questionnaireId), language);
            map.addAttribute("formLangList", formLangList);
            List<InstructionsLangBO> instructionsLangBOList =
                studyQuestionnaireService.getInstructionLangByQuestionnaireId(
                    Integer.parseInt(questionnaireId), language);
            map.addAttribute("instructionsLangBOList", instructionsLangBOList);
          }

          boolean isAnchorQuestionnaire =
              studyQuestionnaireService.isAnchorDateExistByQuestionnaire(
                  Integer.valueOf(questionnaireId));
          map.addAttribute("isAnchorQuestionnaire", isAnchorQuestionnaire);
        }
        if ("add".equals(actionType)) {
          map.addAttribute("actionType", "add");
          request.getSession().setAttribute(sessionStudyCount + "actionType", "add");
        }
        map.addAttribute("_S", sessionStudyCount);
        mav = new ModelAndView("questionnairePage", map);
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - getQuestionnairePage - Error", e);
    }
    logger.info("StudyQuestionnaireController - getQuestionnairePage - Ends");
    return mav;
  }

  /**
   * Load the Question step page in questionnaire which contains the question and answer. Which
   * Carries one QA per screen in Mobile app
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/questionStep.do")
  public ModelAndView getQuestionStepPage(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - getQuestionStepPage - starts");
    ModelAndView mav = new ModelAndView("questionStepPage");
    String sucMsg = "";
    String errMsg = "";
    ModelMap map = new ModelMap();
    QuestionnaireBo questionnaireBo = null;
    QuestionnairesStepsBo questionnairesStepsBo = null;
    List<GroupMappingBo> groupStepLists = new ArrayList<>();
    StudyBo studyBo = null;
    List<String> timeRangeList = new ArrayList<>();
    List<GroupsBo> groupsListPreLoad = null;
    List<GroupsBo> groupsListPostLoad = null;
    GroupsBo groupsBo = null;
    List<StatisticImageListBo> statisticImageList = new ArrayList<>();
    List<ActivetaskFormulaBo> activetaskFormulaList = new ArrayList<>();
    List<QuestionResponseTypeMasterInfoBo> questionResponseTypeMasterInfoList = new ArrayList<>();
    List<HealthKitKeysInfo> healthKitKeysInfo = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG)) {
          sucMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, sucMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
        }
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG)) {
          errMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.ERR_MSG, errMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
        }
        String questionId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionId"))
                ? ""
                : request.getParameter("questionId");
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        String permission =
            (String) request.getSession().getAttribute(sessionStudyCount + "permission");
        request.getSession().removeAttribute(sessionStudyCount + "actionTypeForQuestionPage");
        String actionType =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionType"))
                ? ""
                : request.getParameter("actionType");
        if (StringUtils.isEmpty(actionType)) {
          actionType = (String) request.getSession().getAttribute(sessionStudyCount + "actionType");
        }
        String isLive =
                (String)
                        request
                                .getSession()
                                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.IS_LIVE);
        map.addAttribute("nav", request.getParameter("nav"));
        String actionTypeForQuestionPage =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionTypeForQuestionPage"))
                ? ""
                : request.getParameter("actionTypeForQuestionPage");
        if (StringUtils.isEmpty(actionTypeForQuestionPage)) {
          actionTypeForQuestionPage =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + "actionTypeForQuestionPage");
          if ("edit".equals(actionTypeForQuestionPage)) {
            map.addAttribute("actionTypeForQuestionPage", "edit");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "edit");
          } else if ("view".equals(actionTypeForQuestionPage)) {
            map.addAttribute("actionTypeForQuestionPage", "view");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "view");
          } else {
            map.addAttribute("actionTypeForQuestionPage", "add");
            request
                .getSession()
                .setAttribute(sessionStudyCount + "actionTypeForQuestionPage", "add");
          }
        } else {
          map.addAttribute("actionTypeForQuestionPage", actionTypeForQuestionPage);
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + "actionTypeForQuestionPage", actionTypeForQuestionPage);
        }

        if (StringUtils.isEmpty(studyId)) {
          studyId =
              FdahpStudyDesignerUtil.isEmpty(
                      request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                  ? ""
                  : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
          request
              .getSession()
              .setAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID, studyId);
        }
        if (StringUtils.isNotEmpty(studyId)) {
          studyBo = studyService.getStudyById(studyId, sesObj.getUserId());
          /*boolean isExists = studyQuestionnaireService
          		.isAnchorDateExistsForStudy(
          				Integer.valueOf(studyId),
          				studyBo.getCustomStudyId());
          map.addAttribute("isAnchorDate", isExists);*/
          map.addAttribute(FdahpStudyDesignerConstants.STUDY_BO, studyBo);
          String languages = studyBo.getSelectedLanguages();
          List<String> langList = new ArrayList<>();
          Map<String, String> langMap = new HashMap<>();
          if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
            langList = Arrays.asList(languages.split(","));
            for (String string : langList) {
              langMap.put(string, MultiLanguageCodes.getValue(string));
            }
          }
          map.addAttribute("languageList", langMap);
        }
        String language = request.getParameter("language");
        if (FdahpStudyDesignerUtil.isNotEmpty(language)
            && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
          this.setStudyLangData(studyId, language, map);
        }
        map.addAttribute("currLanguage", language);
        map.addAttribute("isAutoSaved", request.getParameter("isAutoSaved"));
        if (StringUtils.isEmpty(questionId)) {
          questionId = (String) request.getSession().getAttribute(sessionStudyCount + "questionId");
          request.getSession().setAttribute(sessionStudyCount + "questionId", questionId);
        }
        if (StringUtils.isEmpty(questionnaireId)) {
          questionnaireId =
              (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
        }
        if (StringUtils.isNotEmpty(questionnaireId)) {
          request.getSession().removeAttribute(sessionStudyCount + "actionType");
          questionnaireBo =
              studyQuestionnaireService.getQuestionnaireById(
                  Integer.valueOf(questionnaireId), studyBo.getCustomStudyId());
          map.addAttribute("questionnaireBo", questionnaireBo);
          if (questionnaireBo != null && StringUtils.isNotEmpty(questionnaireBo.getFrequency())) {
            String frequency = questionnaireBo.getFrequency();
            if (questionnaireBo
                .getFrequency()
                .equalsIgnoreCase(FdahpStudyDesignerConstants.FREQUENCY_TYPE_DAILY)) {
              if (questionnaireBo.getQuestionnairesFrequenciesList() != null
                  && questionnaireBo.getQuestionnairesFrequenciesList().size() > 1) {
                frequency = questionnaireBo.getFrequency();
              } else {
                frequency = FdahpStudyDesignerConstants.FREQUENCY_TYPE_ONE_TIME;
              }
            }
            timeRangeList = FdahpStudyDesignerUtil.getTimeRangeList(frequency);
          }
          if ("edit".equals(actionType)) {
            map.addAttribute("actionType", "edit");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "edit");
          } else if ("view".equals(actionType)) {
            map.addAttribute("actionType", "view");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "view");
          } else {
            map.addAttribute("actionType", "add");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "add");
          }
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
        }
        if (questionId != null && !questionId.isEmpty()) {
          questionnairesStepsBo =
              studyQuestionnaireService.getQuestionnaireStep(
                  Integer.valueOf(questionId),
                  FdahpStudyDesignerConstants.QUESTION_STEP,
                  questionnaireBo.getShortTitle(),
                  studyBo.getCustomStudyId(),
                  questionnaireBo.getId());
//          if (questionnairesStepsBo != null) {
//            List<QuestionnairesStepsBo> destionationStepList =
//                studyQuestionnaireService.getQuestionnairesStepsList(
//                    questionnairesStepsBo.getQuestionnairesId(),
//                    questionnairesStepsBo.getSequenceNo());
//            map.addAttribute("destinationStepList", destionationStepList);
//          }

          QuestionLangBO spanishQuestionBo = studyQuestionnaireService.getQuestionLangBO(Integer.parseInt(questionId), "es");
          if (spanishQuestionBo != null) {
            String[] displayText = StringUtils.isNotBlank(spanishQuestionBo.getDisplayText())
                    ? spanishQuestionBo.getDisplayText().split("\\|")
                    : null;
            String[] description = StringUtils.isNotBlank(spanishQuestionBo.getTextChoiceDescription())
                    ? spanishQuestionBo.getTextChoiceDescription().split("\\|")
                    : null;
            int i = 0;
            if (questionnairesStepsBo != null && questionnairesStepsBo.getQuestionResponseSubTypeList() != null) {
              for (QuestionResponseSubTypeBo subTypeBo : questionnairesStepsBo.getQuestionResponseSubTypeList()) {
                if (subTypeBo != null) {
                  subTypeBo.setDisplayTextLang(displayText != null
                          ? (displayText.length > i ? displayText[i] : "")
                          : null);
                  subTypeBo.setDescriptionLang(description != null
                          ? (description.length > i ? description[i] : "")
                          : null);
                }
                i++;
              }
            }
          }

          map.addAttribute("questionnairesStepsBo", questionnairesStepsBo);
          request.getSession().setAttribute(sessionStudyCount + "questionId", questionId);

          QuestionLangBO questionLangBO =
              studyQuestionnaireService.getQuestionLangBO(Integer.parseInt(questionId), language);
          map.addAttribute("questionLangBO", questionLangBO);
        }
        int pipingQueId = StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : 0;
        int preloadQueId = StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : 0;
        int pipingSeqNo = questionnairesStepsBo != null ? questionnairesStepsBo.getSequenceNo() : 0;
        int preloadSeqNo = questionnairesStepsBo != null ? questionnairesStepsBo.getSequenceNo() : 0;
        Integer currStepId = questionnairesStepsBo != null ? questionnairesStepsBo.getStepId() : null;
        if (questionnairesStepsBo != null) {
          if (questionnairesStepsBo.getDifferentSurvey() != null
                  && questionnairesStepsBo.getDifferentSurvey()
                  && questionnairesStepsBo.getPipingSurveyId() != null) {
            pipingQueId = questionnairesStepsBo.getPipingSurveyId();
            pipingSeqNo = -1;
          }
          if (questionnairesStepsBo.getDifferentSurveyPreLoad() != null
                  && questionnairesStepsBo.getDifferentSurveyPreLoad()
                  && questionnairesStepsBo.getPreLoadSurveyId() != null) {
            preloadQueId = questionnairesStepsBo.getPreLoadSurveyId();
            preloadSeqNo = -1;
          }
          List<QuestionnairesStepsBo> stepsBoList = studyQuestionnaireService
                  .getSameSurveySourceKeys(preloadQueId, preloadSeqNo, "preload", currStepId,
                          StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : null);
          map.addAttribute("sameSurveyPreloadSourceKeys", stepsBoList);

          List<QuestionnairesStepsBo> destinationStepList = studyQuestionnaireService
                  .getPostLoadDestinationKeys(StringUtils.isNotBlank(questionnaireId) ? Integer.parseInt(questionnaireId) : null,
                          questionnairesStepsBo.getSequenceNo(),
                          currStepId);
          map.addAttribute("destinationStepList", destinationStepList);
        } else {
          pipingSeqNo = -1;
        }
        map.addAttribute("sameSurveyPipingSourceKeys", studyQuestionnaireService
                .getSameSurveySourceKeys(pipingQueId, pipingSeqNo, "piping", currStepId, null));

        String queIdForGroups = questionnaireId;
        Integer stepId = questionnairesStepsBo != null ? questionnairesStepsBo.getInstructionFormId() : null;
        if (questionnairesStepsBo != null && questionnairesStepsBo.getDifferentSurveyPreLoad() != null
                && questionnairesStepsBo.getDifferentSurveyPreLoad()
                && questionnairesStepsBo.getPreLoadSurveyId() != null) {
          queIdForGroups = String.valueOf(questionnairesStepsBo.getPreLoadSurveyId());
        }
        if (StringUtils.isNotEmpty(questionnaireId)) {
          groupsListPreLoad = studyQuestionnaireService.getGroupsForPreloadAndPostLoad(questionnaireId, queIdForGroups, stepId, true);
          groupsListPostLoad = studyQuestionnaireService.getGroupsForPreloadAndPostLoad(questionnaireId, null, stepId, false);
        }
        map.addAttribute("groupsListPostLoad", groupsListPostLoad);
        map.addAttribute("groupsListPreLoad", groupsListPreLoad);
        if (studyBo != null && FdahpStudyDesignerConstants.YES.equals(isLive)) {
          studyId = studyBo.getCustomStudyId();
        }
        map.addAttribute("questionnaireIds", studyQuestionnaireService.getQuestionnairesForPiping(questionnaireId, studyId, FdahpStudyDesignerConstants.YES.equals(isLive)));
        map.addAttribute("operators", this.getOperatorsListByResponseType(questionnairesStepsBo != null ?
                questionnairesStepsBo.getQuestionsBo().getResponseType() : null));
        statisticImageList = studyActiveTasksService.getStatisticImages();
        activetaskFormulaList = studyActiveTasksService.getActivetaskFormulas();
        questionResponseTypeMasterInfoList = studyQuestionnaireService.getQuestionReponseTypeList();
        if (studyBo != null) {
          if (studyBo.getPlatform().contains(FdahpStudyDesignerConstants.IOS)) {
            healthKitKeysInfo = studyQuestionnaireService.getHeanlthKitKeyInfoList();
            map.addAttribute("healthKitKeysInfo", healthKitKeysInfo);
          }
        }
        Integer questionStepId = questionnairesStepsBo != null ? questionnairesStepsBo.getStepId() : null;
        //getting groupId by sending questionstepId from GroupMapping table and getting the groupsBo for that particular groupId
        groupsBo = studyQuestionnaireService.getGroupIdBySendingQuestionStepId(questionStepId);
        map.addAttribute("groupsBo", groupsBo);

        if(groupsBo != null) {
          Integer id = groupsBo.getId();
          // Skippable should be false and non-editable for last step in a group if group level pre-load logic is enabled
          //getting Assigned stepList for particular groupId by sending questionStepId
          if (!"".equals(id)) {
            groupStepLists = studyQuestionnaireService.getStepId(String.valueOf(id), questionnaireId);
          }
        }
        GroupMappingBo lastStepObject;
        boolean IsSkippableFlag = false;
        if(groupStepLists.size() != 0) {
          lastStepObject = groupStepLists.get(groupStepLists.size() - 1);
          System.out.println(lastStepObject.getQuestionnaireStepId());
          System.out.println(questionStepId);
          if(lastStepObject.getQuestionnaireStepId().equals(questionStepId)){
            IsSkippableFlag = true;
          }
        }

        map.addAttribute("IsSkippableFlag", IsSkippableFlag);
        map.addAttribute("permission", permission);
        map.addAttribute("timeRangeList", timeRangeList);
        map.addAttribute("statisticImageList", statisticImageList);
        map.addAttribute("activetaskFormulaList", activetaskFormulaList);
        map.addAttribute("questionnaireId", questionnaireId);
        map.addAttribute("questionResponseTypeMasterInfoList", questionResponseTypeMasterInfoList);
        map.addAttribute("_S", sessionStudyCount);
        mav = new ModelAndView("questionStepPage", map);
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - getQuestionStepPage - Error", e);
    }
    logger.info("StudyQuestionnaireController - getQuestionStepPage - Ends");
    return mav;
  }

  /**
   * From step contains the list of questions with default admin created master order.Admin can
   * manage these orders by reordering the question on drag and drop of a questions in the list
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String : Success/Failure
   */
  @RequestMapping(value = "/adminStudies/reOrderFormQuestions.do", method = RequestMethod.POST)
  public void reOrderFromStepQuestionsInfo(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - reOrderQuestionnaireStepInfo - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      int oldOrderNumber;
      int newOrderNumber;
      if (sesObj != null) {
        String formId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("formId"))
                ? ""
                : request.getParameter("formId");
        String oldOrderNo =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("oldOrderNumber"))
                ? ""
                : request.getParameter("oldOrderNumber");
        String newOrderNo =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("newOrderNumber"))
                ? ""
                : request.getParameter("newOrderNumber");
        if (!formId.isEmpty() && !oldOrderNo.isEmpty() && !newOrderNo.isEmpty()) {
          oldOrderNumber = Integer.valueOf(oldOrderNo);
          newOrderNumber = Integer.valueOf(newOrderNo);
          message =
              studyQuestionnaireService.reOrderFormStepQuestions(
                  Integer.valueOf(formId), oldOrderNumber, newOrderNumber);
          if (message.equalsIgnoreCase(FdahpStudyDesignerConstants.SUCCESS)) {
            String studyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
            String customStudyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(
                            sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
            if (StringUtils.isNotEmpty(studyId)) {
              studyService.markAsCompleted(
                  Integer.valueOf(studyId),
                  FdahpStudyDesignerConstants.QUESTIONNAIRE,
                  false,
                  sesObj,
                  customStudyId);
            }
          }
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - reOrderQuestionnaireStepInfo - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - reOrderQuestionnaireStepInfo - Ends");
  }

  /**
   * A questionnaire is an ordered set of one or more steps (screens on the mobile app). The
   * questionnaire by default follows the master order of steps admin can manage the order of an
   * step.Here we can do the reordering of an questionnaire steps(Instruction,Question,Form) which
   * are listed on questionnaire content page.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(
      value = "/adminStudies/reOrderQuestionnaireStepInfo.do",
      method = RequestMethod.POST)
  public void reOrderQuestionnaireStepInfo(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - reOrderQuestionnaireStepInfo - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    Map<Integer, QuestionnaireStepBean> qTreeMap = new TreeMap<Integer, QuestionnaireStepBean>();
    ObjectMapper mapper = new ObjectMapper();
    JSONObject questionnaireJsonObject = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      int oldOrderNumber = 0;
      int newOrderNumber = 0;
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        String oldOrderNo =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("oldOrderNumber"))
                ? ""
                : request.getParameter("oldOrderNumber");
        String newOrderNo =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("newOrderNumber"))
                ? ""
                : request.getParameter("newOrderNumber");
        if ((questionnaireId != null && !questionnaireId.isEmpty())
            && !oldOrderNo.isEmpty()
            && !newOrderNo.isEmpty()) {
          oldOrderNumber = Integer.valueOf(oldOrderNo);
          newOrderNumber = Integer.valueOf(newOrderNo);
          message =
              studyQuestionnaireService.reOrderQuestionnaireSteps(
                  Integer.valueOf(questionnaireId), oldOrderNumber, newOrderNumber);
          if (message.equalsIgnoreCase(FdahpStudyDesignerConstants.SUCCESS)) {
            qTreeMap =
                studyQuestionnaireService.getQuestionnaireStepList(
                    Integer.valueOf(questionnaireId));
            if (qTreeMap != null) {
              boolean isDone = true;
              for (Entry<Integer, QuestionnaireStepBean> entry : qTreeMap.entrySet()) {
                QuestionnaireStepBean questionnaireStepBean = entry.getValue();
                if (questionnaireStepBean.getStatus() != null
                    && !questionnaireStepBean.getStatus()) {
                  isDone = false;
                  break;
                }
                if (entry.getValue().getFromMap() != null) {
                  if (!entry.getValue().getFromMap().isEmpty()) {
                    for (Entry<Integer, QuestionnaireStepBean> entryKey :
                        entry.getValue().getFromMap().entrySet()) {
                      if (!entryKey.getValue().getStatus()) {
                        isDone = false;
                        break;
                      }
                    }
                  } else {
                    isDone = false;
                    break;
                  }
                }
              }
              jsonobject.put("isDone", isDone);
              questionnaireJsonObject = new JSONObject(mapper.writeValueAsString(qTreeMap));
            }
            jsonobject.put("questionnaireJsonObject", questionnaireJsonObject);
            String studyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
            String customStudyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(
                            sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
            if (StringUtils.isNotEmpty(studyId)) {
              studyService.markAsCompleted(
                  Integer.valueOf(studyId),
                  FdahpStudyDesignerConstants.QUESTIONNAIRE,
                  false,
                  sesObj,
                  customStudyId);
            }
          }
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - reOrderQuestionnaireStepInfo - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - reOrderQuestionnaireStepInfo - Ends");
  }

  /**
   * Here admin will add the from step to the questionnaire which contains the two sets of
   * attributes. which are step level attribute,form level attribute. Admin has fill the required
   * fields and click on save request come here
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String : Success or Failure
   */
  @RequestMapping(value = "/adminStudies/saveFromStep.do")
  public void saveFormStep(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - saveFormStep - starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    QuestionnairesStepsBo questionnairesStepsBo = null;
    ObjectMapper mapper = new ObjectMapper();
    QuestionnairesStepsBo addQuestionnairesStepsBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String questionnaireStepInfo = request.getParameter("questionnaireStepInfo");
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        if (null != questionnaireStepInfo) {
          questionnairesStepsBo =
              mapper.readValue(questionnaireStepInfo, QuestionnairesStepsBo.class);
          if (questionnairesStepsBo != null) {
            if (questionnairesStepsBo.getStepId() != null) {
              questionnairesStepsBo.setModifiedBy(sesObj.getUserId());
              questionnairesStepsBo.setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
            } else {
              questionnairesStepsBo.setCreatedBy(sesObj.getUserId());
              questionnairesStepsBo.setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
            }
            String language = request.getParameter("language");
            String studyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
            if (FdahpStudyDesignerUtil.isNotEmpty(language)
                && !"en".equals(language)
                && !"undefined".equals(language)) {
              questionnairesStepsBo.setCreatedBy(sesObj.getUserId());
              String result =
                  studyQuestionnaireService.saveOrUpdateFormStepForOtherLanguages(
                      questionnairesStepsBo, language, studyId);
              if (!FdahpStudyDesignerConstants.SUCCESS.equals(result)) {
                logger.error("Error while saving Form Data");
              } else {
                jsonobject.put("stepId", questionnairesStepsBo.getStepId());
                jsonobject.put("formId", questionnairesStepsBo.getInstructionFormId());
                message = FdahpStudyDesignerConstants.SUCCESS;
              }
            } else {
              addQuestionnairesStepsBo =
                  studyQuestionnaireService.saveOrUpdateFromStepQuestionnaire(
                      questionnairesStepsBo, sesObj, customStudyId, studyId);
              if (addQuestionnairesStepsBo != null) {
                jsonobject.put("stepId", addQuestionnairesStepsBo.getStepId());
                jsonobject.put("formId", addQuestionnairesStepsBo.getInstructionFormId());
                message = FdahpStudyDesignerConstants.SUCCESS;
                if (StringUtils.isNotEmpty(studyId)) {
                  studyService.markAsCompleted(
                      Integer.parseInt(studyId),
                      FdahpStudyDesignerConstants.QUESTIONNAIRE,
                      false,
                      sesObj,
                      customStudyId);
                }
              }
              String isAutoSaved = request.getParameter("isAutoSaved");
              jsonobject.put("isAutoSaved", isAutoSaved);
            }
          }
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveFormStep - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveFormStep - Ends");
  }

  /**
   * Create the instruction step in Questionnaire which lays the instruction to user in mobile
   * app.Admin would needs to fill the short title instruction title and instruction text.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/saveInstructionStep.do")
  public void saveInstructionStep(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - saveInstructionStep - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    InstructionsBo instructionsBo = null;
    ObjectMapper mapper = new ObjectMapper();
    InstructionsBo addInstructionsBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String instructionsInfo = request.getParameter("instructionsInfo");
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        if (null != instructionsInfo) {
          instructionsBo = mapper.readValue(instructionsInfo, InstructionsBo.class);
          if (instructionsBo != null) {
            if (instructionsBo.getId() != null) {
              instructionsBo.setModifiedBy(sesObj.getUserId());
              instructionsBo.setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
            } else {
              instructionsBo.setCreatedBy(sesObj.getUserId());
              instructionsBo.setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
              instructionsBo.setActive(true);
            }
            if (instructionsBo.getQuestionnairesStepsBo() != null) {
              if (instructionsBo.getQuestionnairesStepsBo().getStepId() != null) {
                instructionsBo
                    .getQuestionnairesStepsBo()
                    .setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
                instructionsBo.getQuestionnairesStepsBo().setModifiedBy(sesObj.getUserId());
              } else {
                instructionsBo
                    .getQuestionnairesStepsBo()
                    .setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
                instructionsBo.getQuestionnairesStepsBo().setCreatedBy(sesObj.getUserId());
              }
            }
            String language = request.getParameter("language");
            String pipingObject = request.getParameter("pipingObject");
            QuestionnaireStepBean pipingBean = null;
            if (StringUtils.isNotBlank(pipingObject)) {
              pipingBean = mapper.readValue(pipingObject, QuestionnaireStepBean.class);
            }
            addInstructionsBo =
                studyQuestionnaireService.saveOrUpdateInstructionsBo(
                    instructionsBo, sesObj, customStudyId, language, studyId, pipingBean);
          }
        }
        if (addInstructionsBo != null) {
          jsonobject.put("instructionId", addInstructionsBo.getId());
          jsonobject.put("stepId", addInstructionsBo.getQuestionnairesStepsBo().getStepId());
          message = FdahpStudyDesignerConstants.SUCCESS;
          if (StringUtils.isNotEmpty(studyId)) {
            studyService.markAsCompleted(
                Integer.valueOf(studyId),
                FdahpStudyDesignerConstants.QUESTIONNAIRE,
                false,
                sesObj,
                customStudyId);
          }
        }
        String isAutoSaved = request.getParameter("isAutoSaved");
        jsonobject.put("isAutoSaved", isAutoSaved);
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveInstructionStep - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveInstructionStep - Ends");
  }

  /**
   * Question of a form step contains the two attributes.Question-level attributes-these are the
   * same set of attributes as that for question step with the exception of the skippable property
   * and branching logic based on participant choice of response or the conditional logic based
   * branching Response-level attributes (same as that for Question Step). Here we can save or
   * update the form questions.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/saveOrUpdateFromQuestion.do")
  public ModelAndView saveOrUpdateFormQuestion(
      HttpServletRequest request, HttpServletResponse response, QuestionsBo questionsBo) {
    logger.info("StudyQuestionnaireController - saveOrUpdateFormQuestion - Starts");
    ModelAndView mav = new ModelAndView("instructionsStepPage");
    ModelMap map = new ModelMap();
    QuestionsBo addQuestionsBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      String language = request.getParameter("language");
      String studyId =
          (String)
              request
                  .getSession()
                  .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        if (questionsBo != null) {
          if (questionsBo.getId() != null) {
            questionsBo.setModifiedBy(sesObj.getUserId());
            questionsBo.setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
          } else {
            questionsBo.setCreatedBy(sesObj.getUserId());
            questionsBo.setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
          }
          addQuestionsBo =
              studyQuestionnaireService.saveOrUpdateQuestion(
                  questionsBo, sesObj, customStudyId, language, studyId);
        }
        if (addQuestionsBo != null) {
          if (addQuestionsBo.getId() != null) {
            request
                .getSession()
                .setAttribute(
                    sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                    "Form Question updated successfully.");
          } else {
            request
                .getSession()
                .setAttribute(
                    sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                    "Form Question added successfully.");
          }
          if (StringUtils.isNotEmpty(studyId)
              && (FdahpStudyDesignerUtil.isEmpty(language)
                  || MultiLanguageCodes.ENGLISH.getKey().equals(language))) {
            studyService.markAsCompleted(
                Integer.valueOf(studyId),
                FdahpStudyDesignerConstants.QUESTIONNAIRE,
                false,
                sesObj,
                customStudyId);
          }
          map.addAttribute("_S", sessionStudyCount);
          map.addAttribute("language", language);
          String isAutoSaved = request.getParameter("isAutoSaved");
          map.addAttribute("isAutoSaved", isAutoSaved);
          mav = new ModelAndView("redirect:/adminStudies/formStep.do", map);
        } else {
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG,
                  "Form not added successfully.");
          map.addAttribute("_S", sessionStudyCount);
          mav = new ModelAndView("redirect:/adminStudies/formQuestion.do", map);
        }
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveOrUpdateFormQuestion - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveOrUpdateFormQuestion - Ends");
    return mav;
  }

  /**
   * Here admin will add the from step to the questionnaire which contains the two sets of
   * attributes. which are step level attribute,form level attribute.Admin has fill the required
   * fields and click on done it save the info here.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/saveOrUpdateFromStepQuestionnaire.do")
  public ModelAndView saveOrUpdateFormStepQuestionnaire(
      HttpServletRequest request,
      HttpServletResponse response,
      QuestionnairesStepsBo questionnairesStepsBo) {
    logger.info("StudyQuestionnaireController - saveOrUpdateFormStepQuestionnaire - Starts");
    ModelAndView mav = new ModelAndView("instructionsStepPage");
    ModelMap map = new ModelMap();
    QuestionnairesStepsBo addQuestionnairesStepsBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        if (questionnairesStepsBo != null) {
          if (questionnairesStepsBo.getStepId() != null) {
            questionnairesStepsBo.setModifiedBy(sesObj.getUserId());
            questionnairesStepsBo.setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
          } else {
            questionnairesStepsBo.setCreatedBy(sesObj.getUserId());
            questionnairesStepsBo.setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
          }
          String language = request.getParameter("language");
          String studyId =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
          if (FdahpStudyDesignerUtil.isNotEmpty(language)
              && !"en".equals(language)
              && !"undefined".equals(language)) {
            questionnairesStepsBo.setCreatedBy(sesObj.getUserId());
            String result =
                studyQuestionnaireService.saveOrUpdateFormStepForOtherLanguages(
                    questionnairesStepsBo, language, studyId);
            if (FdahpStudyDesignerConstants.SUCCESS.equals(result)) {
              request
                  .getSession()
                  .setAttribute(
                      sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                      "Form Step updated successfully.");
              map.addAttribute("_S", sessionStudyCount);
              map.addAttribute("language", language);
              String isAutoSaved = request.getParameter("isAutoSaved");
              map.addAttribute("isAutoSaved", isAutoSaved);
              mav = new ModelAndView("redirect:/adminStudies/viewQuestionnaire.do", map);
            } else {
              request
                  .getSession()
                  .setAttribute(
                      sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG,
                      "Form not added successfully.");
              map.addAttribute("_S", sessionStudyCount);
              mav = new ModelAndView("redirect:/adminStudies/formStep.do", map);
              logger.error("Error while saving Form Data");
            }
          } else {
            addQuestionnairesStepsBo =
                studyQuestionnaireService.saveOrUpdateFromStepQuestionnaire(
                    questionnairesStepsBo, sesObj, customStudyId, studyId);
            if (addQuestionnairesStepsBo != null) {
              if (StringUtils.isNotEmpty(studyId)) {
                studyService.markAsCompleted(
                    Integer.parseInt(studyId),
                    FdahpStudyDesignerConstants.QUESTIONNAIRE,
                    false,
                    sesObj,
                    customStudyId);
              }
              if (questionnairesStepsBo.getStepId() != null) {
                request
                    .getSession()
                    .setAttribute(
                        sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                        "Form Step updated successfully.");
              } else {
                request
                    .getSession()
                    .setAttribute(
                        sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                        "Form Step added successfully.");
              }
              map.addAttribute("_S", sessionStudyCount);
              mav = new ModelAndView("redirect:/adminStudies/viewQuestionnaire.do", map);
            } else {
              request
                  .getSession()
                  .setAttribute(
                      sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG,
                      "Form not added successfully.");
              map.addAttribute("_S", sessionStudyCount);
              mav = new ModelAndView("redirect:/adminStudies/formStep.do", map);
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveOrUpdateFormStepQuestionnaire - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveOrUpdateFormStepQuestionnaire - Ends");
    return mav;
  }

  /**
   * Create the instruction step in Questionnaire which lays the instruction to user in mobile
   * app.Admin would needs to fill the short title instruction title and instruction text.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @param instructionsBo {@link InstructionsBo}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/saveOrUpdateInstructionStep.do")
  public ModelAndView saveOrUpdateInstructionStep(
      HttpServletRequest request, HttpServletResponse response, InstructionsBo instructionsBo) {
    logger.info("StudyQuestionnaireController - saveOrUpdateInstructionStep - Starts");
    ModelAndView mav = new ModelAndView("instructionsStepPage");
    ModelMap map = new ModelMap();
    InstructionsBo addInstructionsBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        String language = request.getParameter("language");
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        if (instructionsBo != null) {
          if (instructionsBo.getId() != null) {
            instructionsBo.setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
            instructionsBo.setModifiedBy(sesObj.getUserId());
          } else {
            instructionsBo.setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
            instructionsBo.setCreatedBy(sesObj.getUserId());
          }
          if (instructionsBo.getQuestionnairesStepsBo() != null) {
            if (instructionsBo.getQuestionnairesStepsBo().getStepId() != null) {
              instructionsBo
                  .getQuestionnairesStepsBo()
                  .setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
              instructionsBo.getQuestionnairesStepsBo().setModifiedBy(sesObj.getUserId());
            } else {
              instructionsBo
                  .getQuestionnairesStepsBo()
                  .setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
              instructionsBo.getQuestionnairesStepsBo().setCreatedBy(sesObj.getUserId());
            }
          }
          addInstructionsBo =
              studyQuestionnaireService.saveOrUpdateInstructionsBo(
                  instructionsBo, sesObj, customStudyId, language, studyId, null);
        }
        if (addInstructionsBo != null) {
          if (StringUtils.isNotEmpty(studyId)
              && (FdahpStudyDesignerUtil.isEmpty(language)
                  || MultiLanguageCodes.ENGLISH.getKey().equals(language))) {
            studyService.markAsCompleted(
                Integer.valueOf(studyId),
                FdahpStudyDesignerConstants.QUESTIONNAIRE,
                false,
                sesObj,
                customStudyId);
          }
          if (instructionsBo.getId() != null) {
            request
                .getSession()
                .setAttribute(
                    sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                    FdahpStudyDesignerConstants.INSTRUCTION_UPDATED_SUCCESSFULLY);
          } else {
            request
                .getSession()
                .setAttribute(
                    sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                    FdahpStudyDesignerConstants.INSTRUCTION_ADDED_SUCCESSFULLY);
          }
          map.addAttribute("_S", sessionStudyCount);
          map.addAttribute("language", language);
          mav = new ModelAndView("redirect:/adminStudies/viewQuestionnaire.do", map);
        } else {
          request
              .getSession()
              .setAttribute(
                  FdahpStudyDesignerConstants.ERR_MSG,
                  FdahpStudyDesignerConstants.INSTRUCTION_UPDATED_SUCCESSFULLY);
          map.addAttribute("_S", sessionStudyCount);
          mav = new ModelAndView("redirect:/adminStudies/instructionsStep.do", map);
        }
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveOrUpdateInstructionStep - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveOrUpdateInstructionStep - Ends");
    return mav;
  }

  /**
   * Create or update of questionnaire in study which contains content and scheduling which can be
   * managed by the admin.The questionnaire schedule frequency can be One
   * time,Daily,Weekly,Monthly,Custom and admin has to select any one frequency.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @param questionnaireBo {@link QuestionnaireBo}
   * @return {@link ModelAndView}
   */
  @RequestMapping(
      value = "/adminStudies/saveorUpdateQuestionnaireSchedule.do",
      method = RequestMethod.POST)
  public ModelAndView saveorUpdateQuestionnaireSchedule(
      HttpServletRequest request, HttpServletResponse response, QuestionnaireBo questionnaireBo) {
    logger.info("StudyQuestionnaireController - saveorUpdateQuestionnaireSchedule - Starts");
    ModelAndView mav = new ModelAndView("questionnairePage");
    ModelMap map = new ModelMap();
    QuestionnaireBo addQuestionnaireBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        if (questionnaireBo != null) {
          if (questionnaireBo.getId() != null) {
            questionnaireBo.setModifiedBy(sesObj.getUserId());
            questionnaireBo.setModifiedDate(FdahpStudyDesignerUtil.getCurrentDateTime());
            questionnaireBo.setStatus(true);
            questionnaireBo.setIsChange(1);
          } else {
            questionnaireBo.setCreatedBy(sesObj.getUserId());
            questionnaireBo.setCreatedDate(FdahpStudyDesignerUtil.getCurrentDateTime());
            questionnaireBo.setStatus(true);
            questionnaireBo.setIsChange(1);
          }
          String language = request.getParameter("language");
          addQuestionnaireBo =
              studyQuestionnaireService.saveOrUpdateQuestionnaire(
                  questionnaireBo, sesObj, customStudyId, language);
          
          if (addQuestionnaireBo != null) {
            if (questionnaireBo.getId() != null) {
              request
                  .getSession()
                  .setAttribute(
                      sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                      "Questionnaire Updated successfully.");
            } else {
              request
                  .getSession()
                  .setAttribute(
                      sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                      "Questionnaire added successfully.");
            }
            String studyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
            if (StringUtils.isNotEmpty(studyId)) {
              studyService.markAsCompleted(
                  Integer.valueOf(studyId),
                  FdahpStudyDesignerConstants.QUESTIONNAIRE,
                  false,
                  sesObj,
                  customStudyId);
            }
            map.addAttribute("_S", sessionStudyCount);
            String isAutoSaved = request.getParameter("isAutoSaved");
            map.addAttribute("isAutoSaved", isAutoSaved);
            mav = new ModelAndView("redirect:/adminStudies/viewStudyQuestionnaires.do", map);
          } else {
            request
                .getSession()
                .setAttribute(
                    sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG,
                    "Questionnaire not added successfully.");
            map.addAttribute("_S", sessionStudyCount);
            mav = new ModelAndView("redirect:/adminStudies/viewQuestionnaire.do", map);
          }
        }
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveorUpdateQuestionnaireSchedule - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveorUpdateQuestionnaireSchedule - Ends");
    return mav;
  }

  /**
   * Admin can add the question step to questionnaire here which contains the 3 subsections admin
   * has to fill the sub section such as step level attribute,question level attribute,response
   * level attributes.Questions can be various types as defined by the response format. Depending on
   * the response format, the attributes of the QA would vary Here we can create or update the
   * question step in questionnaire
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/saveOrUpdateQuestionStepQuestionnaire.do")
  public ModelAndView saveOrUpdateQuestionStepQuestionnaire(
      HttpServletRequest request,
      HttpServletResponse response,
      QuestionnairesStepsBo questionnairesStepsBo) {
    logger.info("StudyQuestionnaireController - saveOrUpdateFormStepQuestionnaire - Starts");
    ModelAndView mav = new ModelAndView("instructionsStepPage");
    ModelMap map = new ModelMap();
    QuestionnairesStepsBo addQuestionnairesStepsBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        String language = request.getParameter("language");
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        if (questionnairesStepsBo != null) {
          if (questionnairesStepsBo.getStepId() != null) {
            questionnairesStepsBo.setModifiedBy(sesObj.getUserId());
            questionnairesStepsBo.setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
          } else {
            questionnairesStepsBo.setCreatedBy(sesObj.getUserId());
            questionnairesStepsBo.setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
          }
          addQuestionnairesStepsBo =
              studyQuestionnaireService.saveOrUpdateQuestionStep(
                  questionnairesStepsBo, sesObj, customStudyId, language, studyId, null);
        }
        if (addQuestionnairesStepsBo != null) {
          if (StringUtils.isNotEmpty(studyId)) {
            studyService.markAsCompleted(
                Integer.parseInt(studyId),
                FdahpStudyDesignerConstants.QUESTIONNAIRE,
                false,
                sesObj,
                customStudyId);
          }
          if (questionnairesStepsBo.getStepId() != null) {
            request
                .getSession()
                .setAttribute(
                    sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                    "Question Step updated successfully.");
          } else {
            request
                .getSession()
                .setAttribute(
                    sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                    "Question Step added successfully.");
          }
          map.addAttribute("_S", sessionStudyCount);
          mav = new ModelAndView("redirect:/adminStudies/viewQuestionnaire.do", map);
        } else if (FdahpStudyDesignerUtil.isNotEmpty(language) && !"en".equals(language)) {
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                  "Question Step updated successfully.");
          map.addAttribute("_S", sessionStudyCount);
          map.addAttribute("language", language);
          String isAutoSaved = request.getParameter("isAutoSaved");
          map.addAttribute("isAutoSaved", isAutoSaved);
          mav = new ModelAndView("redirect:/adminStudies/viewQuestionnaire.do", map);
        } else {
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG,
                  "Form not added successfully.");
          map.addAttribute("_S", sessionStudyCount);
          mav = new ModelAndView("redirect:/adminStudies/questionStep.do", map);
        }
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveOrUpdateFormStepQuestionnaire - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveOrUpdateFormStepQuestionnaire - Ends");
    return mav;
  }

  /**
   * Question of a form step contains the two attributes .Question-level attributes-these are the
   * same set of attributes as that for question step with the exception of the skippable property
   * and branching logic based on participant choice of response or the conditional logic based
   * branching Response-level attributes (same as that for Question Step).Here we can save or update
   * the form questions.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String : Success/Failure
   */
  @RequestMapping(value = "/adminStudies/saveQuestion.do")
  public void saveQuestion(
      HttpServletRequest request,
      HttpServletResponse response,
      MultipartHttpServletRequest multipleRequest) {
    logger.info("StudyQuestionnaireController - saveQuestion - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    QuestionsBo questionsBo = null;
    ObjectMapper mapper = new ObjectMapper();
    QuestionsBo addQuestionsBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        String questionnaireStepInfo = request.getParameter("questionInfo");
        Iterator<String> itr = multipleRequest.getFileNames();
        HashMap<String, MultipartFile> fileMap = new HashMap<>();
        while (itr.hasNext()) {
          CommonsMultipartFile mpf = (CommonsMultipartFile) multipleRequest.getFile(itr.next());
          fileMap.put(mpf.getFileItem().getFieldName(), mpf);
        }
        if (null != questionnaireStepInfo) {
          questionsBo = mapper.readValue(questionnaireStepInfo, QuestionsBo.class);
          if (questionsBo != null) {
            if (questionsBo.getId() != null) {
              questionsBo.setModifiedBy(sesObj.getUserId());
              questionsBo.setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
            } else {
              questionsBo.setCreatedBy(sesObj.getUserId());
              questionsBo.setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
            }
            if (questionsBo.getResponseType() != null && questionsBo.getResponseType() == 5) {
              if (questionsBo.getQuestionResponseSubTypeList() != null
                  && !questionsBo.getQuestionResponseSubTypeList().isEmpty()) {
                for (QuestionResponseSubTypeBo questionResponseSubTypeBo :
                    questionsBo.getQuestionResponseSubTypeList()) {
                  String key1 = "imageFile[" + questionResponseSubTypeBo.getImageId() + "]";
                  String key2 = "selectImageFile[" + questionResponseSubTypeBo.getImageId() + "]";
                  if (fileMap != null && fileMap.get(key1) != null) {
                    questionResponseSubTypeBo.setImageFile(fileMap.get(key1));
                  }
                  if (fileMap != null && fileMap.get(key2) != null) {
                    questionResponseSubTypeBo.setSelectImageFile(fileMap.get(key2));
                  }
                }
              }
            }
            if (questionsBo.getResponseType() != null
                && questionsBo.getQuestionReponseTypeBo() != null) {
              if (fileMap != null && fileMap.get("minImageFile") != null) {
                questionsBo.getQuestionReponseTypeBo().setMinImageFile(fileMap.get("minImageFile"));
              }
              if (fileMap != null && fileMap.get("maxImageFile") != null) {
                questionsBo.getQuestionReponseTypeBo().setMaxImageFile(fileMap.get("maxImageFile"));
              }
            }
            String language = request.getParameter("language");
            addQuestionsBo =
                studyQuestionnaireService.saveOrUpdateQuestion(
                    questionsBo, sesObj, customStudyId, language, studyId);
          }
        }
        if (addQuestionsBo != null) {
          jsonobject.put("questionId", addQuestionsBo.getId());
          if (addQuestionsBo.getQuestionReponseTypeBo() != null) {
            jsonobject.put(
                "questionResponseId",
                addQuestionsBo.getQuestionReponseTypeBo().getResponseTypeId());
            jsonobject.put(
                "questionsResponseTypeId",
                addQuestionsBo.getQuestionReponseTypeBo().getQuestionsResponseTypeId());
          }
          message = FdahpStudyDesignerConstants.SUCCESS;
          if (StringUtils.isNotEmpty(studyId)) {
            studyService.markAsCompleted(
                Integer.valueOf(studyId),
                FdahpStudyDesignerConstants.QUESTIONNAIRE,
                false,
                sesObj,
                customStudyId);
          }
        }
        String isAutoSaved = request.getParameter("isAutoSaved");
        jsonobject.put("isAutoSaved", isAutoSaved);
      }
      if (FdahpStudyDesignerConstants.SUCCESS.equals(message)) {
        request.getSession().setAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG, "Content saved as draft.");
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveQuestion - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveQuestion - Ends");
  }

  /**
   * Create or update of Questionnaire in study which contains content and scheduling which can be
   * managed.The Questionnaire schedule can be One time, Daily,Weekly,Monthly,Custom.The schedule
   * decides how often the user needs to take it
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/saveQuestionnaireSchedule.do", method = RequestMethod.POST)
  public void saveQuestionnaireSchedule(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - saveQuestionnaireSchedule - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    QuestionnaireBo updateQuestionnaireBo = null;
    ObjectMapper mapper = new ObjectMapper();
    QuestionnaireBo questionnaireBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String questionnaireScheduleInfo = request.getParameter("questionnaireScheduleInfo");
        if (questionnaireScheduleInfo != null && !questionnaireScheduleInfo.isEmpty()) {
          questionnaireBo = mapper.readValue(questionnaireScheduleInfo, QuestionnaireBo.class);
          if (questionnaireBo != null) {
            String studyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
            if (questionnaireBo.getId() != null) {
              questionnaireBo.setModifiedBy(sesObj.getUserId());
              questionnaireBo.setModifiedDate(FdahpStudyDesignerUtil.getCurrentDateTime());
              if (questionnaireBo.getStatus()) {
                request
                    .getSession()
                    .setAttribute(
                        sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                        "Questionnaire Updated successfully.");
              }
            } else {
              questionnaireBo.setCreatedBy(sesObj.getUserId());
              questionnaireBo.setCreatedDate(FdahpStudyDesignerUtil.getCurrentDateTime());
              if (questionnaireBo.getStatus()) {
                request
                    .getSession()
                    .setAttribute(
                        sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                        "Questionnaire added successfully.");
              }
            }
            customStudyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(
                            sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
            String language = request.getParameter("language");
            updateQuestionnaireBo =
                studyQuestionnaireService.saveOrUpdateQuestionnaire(
                    questionnaireBo, sesObj, customStudyId, language);
            if (updateQuestionnaireBo != null) {
              jsonobject.put("questionnaireId", updateQuestionnaireBo.getId());
              if (updateQuestionnaireBo.getQuestionnairesFrequenciesBo() != null) {
                jsonobject.put(
                    "questionnaireFrequenceId",
                    updateQuestionnaireBo.getQuestionnairesFrequenciesBo().getId());
              }
              if (StringUtils.isNotEmpty(studyId)
                  && (FdahpStudyDesignerUtil.isEmpty(language)
                      || MultiLanguageCodes.ENGLISH.getKey().equals(language))) {
                studyService.markAsCompleted(
                    Integer.valueOf(studyId),
                    FdahpStudyDesignerConstants.QUESTIONNAIRE,
                    false,
                    sesObj,
                    customStudyId);
              }
              message = FdahpStudyDesignerConstants.SUCCESS;
            }
          }
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveQuestionnaireSchedule - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveQuestionnaireSchedule - Ends");
  }

  /**
   * Admin can add the question step to questionnaire here which contains the 3 subsections admin
   * has to fill the sub section such as step level attribute,question level attribute,response
   * level attributes.Questions can be various types as defined by the response format. Depending on
   * the response format, the attributes of the QA would vary
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/saveQuestionStep.do", method = RequestMethod.POST)
  public void saveQuestionStep(
      HttpServletResponse response,
      MultipartHttpServletRequest multipleRequest,
      HttpServletRequest request) {
    logger.info("StudyQuestionnaireController - saveQuestionStep - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    QuestionnairesStepsBo questionnairesStepsBo = null;
    ObjectMapper mapper = new ObjectMapper();
    QuestionnairesStepsBo addQuestionnairesStepsBo = null;
    String customStudyId = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              multipleRequest.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String questionnaireStepInfo = multipleRequest.getParameter("questionnaireStepInfo");
        customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        Iterator<String> itr = multipleRequest.getFileNames();
        HashMap<String, MultipartFile> fileMap = new HashMap<>();
        while (itr.hasNext()) {
          CommonsMultipartFile mpf = (CommonsMultipartFile) multipleRequest.getFile(itr.next());
          fileMap.put(mpf.getFileItem().getFieldName(), mpf);
        }
        String language = request.getParameter("language");
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        if (null != questionnaireStepInfo) {
          questionnairesStepsBo =
              mapper.readValue(questionnaireStepInfo, QuestionnairesStepsBo.class);
          if (questionnairesStepsBo != null) {
            if (questionnairesStepsBo.getStepId() != null) {
              questionnairesStepsBo.setModifiedBy(sesObj.getUserId());
              questionnairesStepsBo.setModifiedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
            } else {
              questionnairesStepsBo.setCreatedBy(sesObj.getUserId());
              questionnairesStepsBo.setCreatedOn(FdahpStudyDesignerUtil.getCurrentDateTime());
            }
          }
          if (questionnairesStepsBo.getQuestionsBo() != null
              && questionnairesStepsBo.getQuestionsBo().getResponseType() != null) {
            if (questionnairesStepsBo.getQuestionsBo().getResponseType() == 5) {
              if (questionnairesStepsBo.getQuestionResponseSubTypeList() != null
                  && !questionnairesStepsBo.getQuestionResponseSubTypeList().isEmpty()) {
                for (QuestionResponseSubTypeBo questionResponseSubTypeBo :
                    questionnairesStepsBo.getQuestionResponseSubTypeList()) {
                  String key1 = "imageFile[" + questionResponseSubTypeBo.getImageId() + "]";
                  String key2 = "selectImageFile[" + questionResponseSubTypeBo.getImageId() + "]";
                  if (fileMap != null && fileMap.get(key1) != null) {
                    questionResponseSubTypeBo.setImageFile(fileMap.get(key1));
                  }
                  if (fileMap != null && fileMap.get(key2) != null) {
                    questionResponseSubTypeBo.setSelectImageFile(fileMap.get(key2));
                  }
                }
              }
            }
            if (questionnairesStepsBo.getQuestionReponseTypeBo() != null) {
              if (fileMap != null && fileMap.get("minImageFile") != null) {
                questionnairesStepsBo
                    .getQuestionReponseTypeBo()
                    .setMinImageFile(fileMap.get("minImageFile"));
              }
              if (fileMap != null && fileMap.get("maxImageFile") != null) {
                questionnairesStepsBo
                    .getQuestionReponseTypeBo()
                    .setMaxImageFile(fileMap.get("maxImageFile"));
              }
            }
          }
          String pipingObject = multipleRequest.getParameter("pipingObject");
          QuestionnaireStepBean pipingBean = null;
          if (StringUtils.isNotBlank(pipingObject)) {
            pipingBean = mapper.readValue(pipingObject, QuestionnaireStepBean.class);
          }
          addQuestionnairesStepsBo =
              studyQuestionnaireService.saveOrUpdateQuestionStep(
                  questionnairesStepsBo, sesObj, customStudyId, language, studyId, pipingBean);
        }
        if (addQuestionnairesStepsBo != null) {
          jsonobject.put("stepId", addQuestionnairesStepsBo.getStepId());
          if (StringUtils.isNotEmpty(studyId)) {
            studyService.markAsCompleted(
                Integer.parseInt(studyId),
                FdahpStudyDesignerConstants.QUESTIONNAIRE,
                false,
                sesObj,
                customStudyId);
          }
          if (addQuestionnairesStepsBo.getQuestionsBo() != null) {
            jsonobject.put("questionId", addQuestionnairesStepsBo.getQuestionsBo().getId());
          }
          String isAutoSaved = request.getParameter("isAutoSaved");
          jsonobject.put("isAutoSaved", isAutoSaved);
          if (addQuestionnairesStepsBo.getQuestionReponseTypeBo() != null) {
            jsonobject.put(
                "questionResponseId",
                addQuestionnairesStepsBo.getQuestionReponseTypeBo().getResponseTypeId());
            jsonobject.put(
                "questionsResponseTypeId",
                addQuestionnairesStepsBo.getQuestionReponseTypeBo().getQuestionsResponseTypeId());
          }
          message = FdahpStudyDesignerConstants.SUCCESS;
        } else if (FdahpStudyDesignerUtil.isNotEmpty(language) && !"en".equals(language)) {
          if (questionnairesStepsBo != null) {
            jsonobject.put("stepId", questionnairesStepsBo.getStepId());
            if (StringUtils.isNotEmpty(studyId)) {
              studyService.markAsCompleted(
                  Integer.parseInt(studyId),
                  FdahpStudyDesignerConstants.QUESTIONNAIRE,
                  false,
                  sesObj,
                  customStudyId);
            }
            if (questionnairesStepsBo.getQuestionsBo() != null) {
              jsonobject.put("questionId", questionnairesStepsBo.getQuestionsBo().getId());
            }
            if (questionnairesStepsBo.getQuestionReponseTypeBo() != null) {
              jsonobject.put(
                  "questionResponseId",
                  questionnairesStepsBo.getQuestionReponseTypeBo().getResponseTypeId());
              jsonobject.put(
                  "questionsResponseTypeId",
                  questionnairesStepsBo.getQuestionReponseTypeBo().getQuestionsResponseTypeId());
            }
            message = FdahpStudyDesignerConstants.SUCCESS;
          }
        }
      }
      if (FdahpStudyDesignerConstants.SUCCESS.equals(message)) {
        request.getSession().setAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG, "Content saved as draft.");
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - saveQuestionStep - Error", e);
    }
    logger.info("StudyQuestionnaireController - saveQuestionStep - Ends");
  }

  /**
   * For QA of response type that results in the data type 'double',the admin can define conditional
   * logic (formula-based) to evaluate with user response as the input. A condition or formula is to
   * be defined along with a destination step to navigate to if the result of evaluation is TRUE and
   * an alternative destination step if FALSE.Admin can check the condition is valid or not here.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(
      value = "/adminStudies/validateconditionalFormula.do",
      method = RequestMethod.POST)
  public void validateconditionalFormula(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - validateconditionalFormula - Starts");
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    ObjectMapper mapper = new ObjectMapper();
    JSONObject formulaResponseJsonObject = null;
    FormulaInfoBean formulaInfoBean = null;
    String message = FdahpStudyDesignerConstants.FAILURE;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      if (sesObj != null) {
        String left_input =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("left_input"))
                ? ""
                : request.getParameter("left_input");
        String right_input =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("right_input"))
                ? ""
                : request.getParameter("right_input");
        String oprator_input =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("oprator_input"))
                ? ""
                : request.getParameter("oprator_input");
        String trialInputVal =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("trialInput"))
                ? ""
                : request.getParameter("trialInput");
        if (!left_input.isEmpty()
            && !right_input.isEmpty()
            && !oprator_input.isEmpty()
            && !trialInputVal.isEmpty()) {
          formulaInfoBean =
              studyQuestionnaireService.validateQuestionConditionalBranchingLogic(
                  left_input, right_input, oprator_input, trialInputVal);
          if (formulaInfoBean != null) {
            formulaResponseJsonObject = new JSONObject(mapper.writeValueAsString(formulaInfoBean));
            jsonobject.put("formulaResponseJsonObject", formulaResponseJsonObject);
            if (formulaInfoBean.getMessage().equalsIgnoreCase(FdahpStudyDesignerConstants.SUCCESS))
              message = FdahpStudyDesignerConstants.SUCCESS;
          }
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - validateconditionalFormula - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - validateconditionalFormula - Ends");
  }

  /**
   * The admin can choose to add a response data element to the study dashboard in the form of line
   * charts or statistics.Adding a line chart to the dashboard needs the admin to specify The
   * options time range for the chart which depend on the scheduling frequency set for the
   * activity.when admin change the frequency in questionnaire schedule its validate the options in
   * the time range for chart options.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/validateLineChartSchedule.do", method = RequestMethod.POST)
  public void validateQuestionnaireLineChartSchedule(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - validateQuestionnaireLineChartSchedule - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    ObjectMapper mapper = new ObjectMapper();
    JSONObject questionnaireJsonObject = null;
    Map<Integer, QuestionnaireStepBean> qTreeMap = new TreeMap<Integer, QuestionnaireStepBean>();
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      if (sesObj != null) {
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        String frequency =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("frequency"))
                ? ""
                : request.getParameter("frequency");
        if (!questionnaireId.isEmpty() && !frequency.isEmpty()) {
          message =
              studyQuestionnaireService.validateLineChartSchedule(
                  Integer.valueOf(questionnaireId), frequency);
          if (message.equalsIgnoreCase(FdahpStudyDesignerConstants.SUCCESS)) {
            qTreeMap =
                studyQuestionnaireService.getQuestionnaireStepList(
                    Integer.valueOf(questionnaireId));
            questionnaireJsonObject = new JSONObject(mapper.writeValueAsString(qTreeMap));
            jsonobject.put("questionnaireJsonObject", questionnaireJsonObject);
          }
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error(
          "StudyQuestionnaireController - validateQuestionnaireLineChartSchedule - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - validateQuestionnaireLineChartSchedule - Ends");
  }

  /**
   * Questionnaire contains the content,schedule as two tabs.Each questionnaire contains the short
   * title in content tab this will be created as the column for the questionnaire response in
   * response server for this we are doing the unique title validation for each questionnaire in
   * study level
   *
   * @author BTC
   * @param request {@link HttpServletRequest}\
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/validateQuestionnaireKey.do", method = RequestMethod.POST)
  public void validateQuestionnaireShortTitle(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - validateQuestionnaireShortTitle - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        String customStudyId =
            FdahpStudyDesignerUtil.isEmpty(
                    request.getParameter(FdahpStudyDesignerConstants.CUSTOM_STUDY_ID))
                ? ""
                : request.getParameter(FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        if (StringUtils.isEmpty(studyId)) {
          studyId =
              FdahpStudyDesignerUtil.isEmpty(
                      request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                  ? ""
                  : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
        }
        if (StringUtils.isEmpty(customStudyId)) {
          customStudyId =
              (String)
                  request
                      .getSession()
                      .getAttribute(
                          sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        }
        String shortTitle =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("shortTitle"))
                ? ""
                : request.getParameter("shortTitle");
        if ((studyId != null && !studyId.isEmpty()) && !shortTitle.isEmpty()) {
          message =
              studyQuestionnaireService.checkQuestionnaireShortTitle(
                  Integer.valueOf(studyId), shortTitle, customStudyId);
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - validateQuestionnaireShortTitle - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - validateQuestionnaireShortTitle - Ends");
  }

  /**
   * A questionnaire is an ordered set of one or more steps.Each step contains the step short title
   * field. Which will be response column for the step in response server.so it should be the
   * unique.Here validating the unique for step short title
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(
      value = "/adminStudies/validateQuestionnaireStepKey.do",
      method = RequestMethod.POST)
  public void validateQuestionnaireStepShortTitle(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - validateQuestionnaireStepShortTitle - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        String stepType =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("stepType"))
                ? ""
                : request.getParameter("stepType");
        String shortTitle =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("shortTitle"))
                ? ""
                : request.getParameter("shortTitle");
        String questionnaireShortTitle =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireShortTitle"))
                ? ""
                : request.getParameter("questionnaireShortTitle");
        String customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        if (StringUtils.isEmpty(customStudyId)) {
          customStudyId =
              FdahpStudyDesignerUtil.isEmpty(
                      request.getParameter(FdahpStudyDesignerConstants.CUSTOM_STUDY_ID))
                  ? ""
                  : request.getParameter(FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        }
        if (!questionnaireId.isEmpty() && !stepType.isEmpty() && !shortTitle.isEmpty()) {
          message =
              studyQuestionnaireService.checkQuestionnaireStepShortTitle(
                  Integer.valueOf(questionnaireId),
                  stepType,
                  shortTitle,
                  questionnaireShortTitle,
                  customStudyId);
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - validateQuestionnaireStepShortTitle - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - validateQuestionnaireStepShortTitle - Ends");
  }

  /**
   * From step have a one or more question.Each question have the short title field this will be
   * created the as column in response server so its should be unique across all the
   * steps.Validateing the Unique of question short title inside form step
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/validateQuestionKey.do", method = RequestMethod.POST)
  public void validateQuestionShortTitle(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - validateQuestionShortTitle - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String questionnaireId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                ? ""
                : request.getParameter("questionnaireId");
        String shortTitle =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("shortTitle"))
                ? ""
                : request.getParameter("shortTitle");
        String questionnaireShortTitle =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireShortTitle"))
                ? ""
                : request.getParameter("questionnaireShortTitle");
        String customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        if (StringUtils.isEmpty(customStudyId)) {
          customStudyId =
              FdahpStudyDesignerUtil.isEmpty(
                      request.getParameter(FdahpStudyDesignerConstants.CUSTOM_STUDY_ID))
                  ? ""
                  : request.getParameter(FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        }
        if (!questionnaireId.isEmpty() && !shortTitle.isEmpty()) {
          message =
              studyQuestionnaireService.checkFromQuestionShortTitle(
                  Integer.valueOf(questionnaireId),
                  shortTitle,
                  questionnaireShortTitle,
                  customStudyId);
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - validateQuestionShortTitle - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - validateQuestionShortTitle - Ends");
  }

  /**
   * The admin can choose to add a response data element to the study dashboard in the form of line
   * charts or statistics.Adding a statistic to the dashboard needs the admin to specify the short
   * name should be unique across all the state in the study So validating the unique validation for
   * short name in states.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/validateStatsShortName.do", method = RequestMethod.POST)
  public void validateQuestionStatsShortTitle(
      HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - validateQuestionStatsShortTitle - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        String customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        String shortTitle =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("shortTitle"))
                ? ""
                : request.getParameter("shortTitle");
        if (!studyId.isEmpty() && !shortTitle.isEmpty()) {
          message =
              studyQuestionnaireService.checkStatShortTitle(
                  Integer.valueOf(studyId), shortTitle, customStudyId);
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - validateQuestionStatsShortTitle - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - validateQuestionStatsShortTitle - Ends");
  }

  /**
   * In Questionnaire form step carries the multiple question and Answers .In form level attributes
   * we can make form form as repeatable if the form is repeatable we can not add the line chart and
   * states data to the dashbord.here we are validating the added line chart and statistics data
   * before updating the form as repeatable.
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(
      value = "/adminStudies/validateRepeatableQuestion.do",
      method = RequestMethod.POST)
  public void validateRepeatableQuestion(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - validateRepeatableQuestion - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      if (sesObj != null) {
        String formId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("formId"))
                ? ""
                : request.getParameter("formId");
        if (!formId.isEmpty()) {
          message =
              studyQuestionnaireService.validateRepetableFormQuestionStats(Integer.valueOf(formId));
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - validateRepeatableQuestion - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - validateRepeatableQuestion - Ends");
  }

  /**
   * List of all the Questionnaires of an study.A Study can have 0 or more Questionnaires and admin
   * can manage a list of questionnaires for the study
   *
   * @author BTC
   * @param request , {@link HttpServletRequest}
   * @return {@link ModelAndView}
   */
  @RequestMapping("/adminStudies/viewStudyQuestionnaires.do")
  public ModelAndView viewStudyQuestionnaires(HttpServletRequest request) {
    logger.info("StudyQuestionnaireController - viewStudyQuestionnaires - Starts");
    ModelAndView mav = new ModelAndView("redirect:viewBasicInfo.do");
    ModelMap map = new ModelMap();
    StudyBo studyBo = null;
    String sucMsg = "";
    String errMsg = "";
    List<QuestionnaireBo> questionnaires = null;
    String activityStudyId = "";
    String customStudyId = "";
    String actMsg = "";
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        request.getSession().removeAttribute(sessionStudyCount + "questionnaireId");
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG)) {
          sucMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, sucMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
        }
        if (null
            != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG)) {
          errMsg =
              (String)
                  request
                      .getSession()
                      .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.ERR_MSG, errMsg);
          request
              .getSession()
              .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
        }

        String studyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        String permission =
            (String) request.getSession().getAttribute(sessionStudyCount + "permission");
        if (StringUtils.isEmpty(studyId)) {
          studyId =
              FdahpStudyDesignerUtil.isEmpty(
                          request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                      == true
                  ? "0"
                  : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
        }
        // Added for live version Start
        String isLive =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.IS_LIVE);
        if (StringUtils.isNotEmpty(isLive)
            && isLive.equalsIgnoreCase(FdahpStudyDesignerConstants.YES)) {
          activityStudyId =
              (String)
                  request
                      .getSession()
                      .getAttribute(
                          sessionStudyCount + FdahpStudyDesignerConstants.QUESTIONNARIE_STUDY_ID);
          customStudyId =
              (String)
                  request
                      .getSession()
                      .getAttribute(
                          sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        }
        // Added for live version End
        Map<String, String> langMap = new HashMap<>();
        if (StringUtils.isNotEmpty(studyId)) {
          request.getSession().removeAttribute(sessionStudyCount + "actionType");
          studyBo = studyService.getStudyById(studyId, sesObj.getUserId());
          if (StringUtils.isNotEmpty(activityStudyId)) {
            questionnaires =
                studyQuestionnaireService.getStudyQuestionnairesByStudyId(customStudyId, true);
          } else {
            questionnaires =
                studyQuestionnaireService.getStudyQuestionnairesByStudyId(studyId, false);
          }
          boolean markAsComplete = true;
          actMsg =
              studyService.validateActivityComplete(
                  studyId, FdahpStudyDesignerConstants.ACTIVITY_TYPE_QUESTIONNAIRE);
          if (!actMsg.equalsIgnoreCase(FdahpStudyDesignerConstants.SUCCESS)) markAsComplete = false;
          map.addAttribute("markAsComplete", markAsComplete);
          if (!markAsComplete) {
            customStudyId =
                (String)
                    request
                        .getSession()
                        .getAttribute(
                            sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
            studyService.markAsCompleted(
                Integer.valueOf(studyId),
                FdahpStudyDesignerConstants.QUESTIONNAIRE,
                false,
                sesObj,
                customStudyId);
          }

          String languages = studyBo.getSelectedLanguages();
          List<String> langList = new ArrayList<>();
          if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
            langList = Arrays.asList(languages.split(","));
            for (String string : langList) {
              langMap.put(string, MultiLanguageCodes.getValue(string));
            }
          }
          map.addAttribute("languageList", langMap);
        }
        String language = request.getParameter("language");
        if (FdahpStudyDesignerUtil.isNotEmpty(language)
            && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
          this.setStudyLangData(studyId, language, map);
          List<QuestionnaireLangBO> questionnaireLangBOS =
              studyQuestionnaireService.syncAndGetQuestionnaireLangList(
                  questionnaires, Integer.parseInt(studyId), language);
          map.addAttribute("questionnaireLangBOS", questionnaireLangBOS);
        }
        map.addAttribute("currLanguage", language);
        map.addAttribute("isAutoSaved", request.getParameter("isAutoSaved"));
        map.addAttribute("languageList", langMap);
        map.addAttribute("permission", permission);
        map.addAttribute(FdahpStudyDesignerConstants.STUDY_BO, studyBo);
        map.addAttribute("questionnaires", questionnaires);
        map.addAttribute(FdahpStudyDesignerConstants.ACTIVITY_MESSAGE, actMsg);
        map.addAttribute("_S", sessionStudyCount);
        mav = new ModelAndView("studyQuestionaryListPage", map);
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - viewStudyQuestionnaires - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - viewStudyQuestionnaires - Ends");
    return mav;
  }

  /**
   * A questionnaire is an ordered set of one or more steps.Each step contains the step short title
   * field. Which will be response column for the step in response server.so it should be the
   * unique.Here validating the unique for step short title
   *
   * @author BTC
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return String Success/Failure
   */
  @RequestMapping(value = "/adminStudies/validateAnchorDateName.do", method = RequestMethod.POST)
  public void validateAnchorDateName(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - validateAnchorDateName - Starts");
    String message = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String anchordateText =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("anchordateText"))
                ? ""
                : request.getParameter("anchordateText");
        String anchorDateId =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("anchorDateId"))
                ? ""
                : request.getParameter("anchorDateId");
        String customStudyId =
            (String)
                request
                    .getSession()
                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        if (StringUtils.isEmpty(customStudyId)) {
          customStudyId =
              FdahpStudyDesignerUtil.isEmpty(
                      request.getParameter(FdahpStudyDesignerConstants.CUSTOM_STUDY_ID))
                  ? ""
                  : request.getParameter(FdahpStudyDesignerConstants.CUSTOM_STUDY_ID);
        }
        if (!anchordateText.isEmpty() && !customStudyId.isEmpty()) {
          message =
              studyQuestionnaireService.checkUniqueAnchorDateName(
                  anchordateText, customStudyId, anchorDateId);
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - validateAnchorDateName - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - validateAnchorDateName - Ends");
  }

  private void setStudyLangData(String studyId, String language, ModelMap map) {
    StudyLanguageBO studyLanguageBO = new StudyLanguageBO();
    StudySequenceLangBO studySequenceLangBO = new StudySequenceLangBO();
    if (FdahpStudyDesignerUtil.isNotEmpty(studyId)) {
      studyLanguageBO = studyService.getStudyLanguageById(Integer.parseInt(studyId), language);
      studySequenceLangBO = studyService.getStudySequenceById(Integer.parseInt(studyId), language);
    }
    map.addAttribute("studyLanguageBO", studyLanguageBO);
    map.addAttribute("sequenceLangBO", studySequenceLangBO);
  }
  
  @RequestMapping(value = "/adminStudies/viewGroups.do")
  public ModelAndView viewGroups(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - viewGroups - Starts");
    ModelAndView mav = new ModelAndView();
    ModelMap map = new ModelMap();
    StudyBo studyBo = null;
    String sucMsg = "";
    String errMsg = "";
    List<GroupsBo> groupsList = null;
    String customStudyId = "";
    String questionnaireId="";
    try {
        SessionObject sesObj =
                (SessionObject)
                        request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
        Integer sessionStudyCount =
                StringUtils.isNumeric(request.getParameter("_S"))
                        ? Integer.parseInt(request.getParameter("_S"))
                        : 0;
        if (sesObj != null
                && sesObj.getStudySession() != null
                && sesObj.getStudySession().contains(sessionStudyCount)) {
          
          if (null
                  != request
                  .getSession()
                  .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG)) {
            sucMsg =
                    (String)
                            request
                                    .getSession()
                                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
            map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, sucMsg);
            request
                    .getSession()
                    .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
          }
          if (null != request.getSession() &&
                  null != request.getSession().getAttribute("gId") &&
                    null != request.getSession().getAttribute("flag")) {
            request.getSession().removeAttribute("gId");
            request.getSession().removeAttribute("flag");
          }
          if (null
                  != request
                  .getSession()
                  .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG)) {
            errMsg =
                    (String)
                            request
                                    .getSession()
                                    .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
            map.addAttribute(FdahpStudyDesignerConstants.ERR_MSG, errMsg);
            request
                    .getSession()
                    .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
          }
          String actionType =
                  FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionType"))
                      ? ""
                      : request.getParameter("actionType");
              if (StringUtils.isEmpty(actionType)) {
                actionType = (String) request.getSession().getAttribute(sessionStudyCount + "actionType");
              }
          String studyId =
                  (String)
                          request
                                  .getSession()
                                  .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
          
          if (StringUtils.isEmpty(studyId)) {
            studyId =
                    FdahpStudyDesignerUtil.isEmpty(
                            request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                            == true
                            ? "0"
                            : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
          }
          String permission =
                  (String) request.getSession().getAttribute(sessionStudyCount + "permission");
          questionnaireId =
                  FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                      ? ""
                      : request.getParameter("questionnaireId");
           if (StringUtils.isEmpty(questionnaireId)) {
        	   questionnaireId =
                   (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
               request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
             }
          
          
          if (StringUtils.isNotEmpty(studyId)) {
            request.getSession().removeAttribute(sessionStudyCount + "actionType");
            studyBo = studyService.getStudyById(studyId, sesObj.getUserId());
            
          }
          if (studyBo == null) {
              studyBo = new StudyBo();
              studyBo.setType(FdahpStudyDesignerConstants.STUDY_TYPE_GT);
              studyBo.setStudyLanguage(FdahpStudyDesignerConstants.STUDY_LANGUAGE_ENGLISH);
            } else if ((studyBo != null) && StringUtils.isNotEmpty(studyBo.getCustomStudyId())) {
              request
                  .getSession()
                  .setAttribute(
                      sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID,
                      studyBo.getCustomStudyId());
            }
          String language = request.getParameter("language");
          if (FdahpStudyDesignerUtil.isNotEmpty(language)
              && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
            this.setStudyLangData(studyId, language, map);
          }

          map.addAttribute("currLanguage", language);
          String languages = studyBo.getSelectedLanguages();
          List<String> langList = new ArrayList<>();
          Map<String, String> langMap = new HashMap<>();
          if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
            langList = Arrays.asList(languages.split(","));
            for (String string : langList) {
              langMap.put(string, MultiLanguageCodes.getValue(string));
            }
          }
          map.addAttribute("languageList", langMap);
          if ("edit".equals(actionType) || "add".equals(actionType)) {
              map.addAttribute("actionType", "edit");
              request.getSession().setAttribute(sessionStudyCount + "actionType", "edit");
            } else {
              map.addAttribute("actionType", "view");
              request.getSession().setAttribute(sessionStudyCount + "actionType", "view");
            }
          if (StringUtils.isNotEmpty(questionnaireId)) {
				groupsList =
                      studyQuestionnaireService.getGroupsByStudyId(studyId,questionnaireId, false, null);
          }
          map.addAttribute("permission", permission);
          map.addAttribute(FdahpStudyDesignerConstants.STUDY_BO, studyBo);
          map.addAttribute("groupsList", groupsList);
          map.addAttribute("_S", sessionStudyCount);
          mav = new ModelAndView("viewGroupsPage", map);
        }
      } catch (Exception e) {
        logger.error("StudyQuestionnaireController - viewGroups - ERROR", e);
      }
      logger.info("StudyQuestionnaireController - viewGroups - Ends");
      return mav;
    

  }

  @RequestMapping(value = "/adminStudies/addOrEditGroupsDetails.do")
  public ModelAndView addOrEditGroupsDetails(HttpServletRequest request) {
    logger.info("UsersController - addOrEditGroupsDetails() - Starts");
    ModelAndView mav = new ModelAndView();
    ModelMap map = new ModelMap();
    GroupsBo groupsBo = null;
    String questionnaireId = "";
    QuestionnaireBo questionnaireBo = null;
    String sucMsg = "";
    StudyBo studyBo = null;
    List<GroupMappingBo> groupStepLists = new ArrayList<>();
    Integer responseType = null;
    List<String> operatorsList = new ArrayList<>();
    List<Integer> questionIdList = new ArrayList<>();
    List<GroupsBo> groupsList = null;
    List<PreLoadLogicBo> preLoadLogicBoList = null;
    Map<Integer, QuestionnaireStepBean> qTreeMap = new TreeMap<Integer, QuestionnaireStepBean>();
    String errMsg = "";
    String actionPage = "";
    int grpId = 0;
    try {
      HttpSession session = request.getSession();
      SessionObject sesObj =
              (SessionObject)
                      session.getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
              StringUtils.isNumeric(request.getParameter("_S"))
                      ? Integer.parseInt(request.getParameter("_S"))
                      : 0;
      if (sesObj != null
              && sesObj.getStudySession() != null
              && sesObj.getStudySession().contains(sessionStudyCount)) {

        if (null
                != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG)) {
          sucMsg =
                  (String)
                          request
                                  .getSession()
                                  .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, sucMsg);
          request
                  .getSession()
                  .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
        }
        if (null
                != request
                .getSession()
                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG)) {
          errMsg =
                  (String)
                          request
                                  .getSession()
                                  .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
          map.addAttribute(FdahpStudyDesignerConstants.ERR_MSG, errMsg);
          request
                  .getSession()
                  .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
        }
        String studyId =
                (String)
                        request
                                .getSession()
                                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);

        if (StringUtils.isEmpty(studyId)) {
          studyId =
                  FdahpStudyDesignerUtil.isEmpty(
                          request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                          == true
                          ? "0"
                          : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
        }
        if (StringUtils.isNotEmpty(studyId)) {
          request.getSession().removeAttribute(sessionStudyCount + "actionType");
          studyBo = studyService.getStudyById(studyId, sesObj.getUserId());

        }
        String language = request.getParameter("language");
        if (FdahpStudyDesignerUtil.isNotEmpty(language)
                && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
          this.setStudyLangData(studyId, language, map);
        }
        map.addAttribute("currLanguage", language);
        String languages = studyBo.getSelectedLanguages();
        List<String> langList = new ArrayList<>();
        Map<String, String> langMap = new HashMap<>();
        if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
          langList = Arrays.asList(languages.split(","));
          for (String string : langList) {
            langMap.put(string, MultiLanguageCodes.getValue(string));
          }
        }
        map.addAttribute("languageList", langMap);

        questionnaireId =
                FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                        ? ""
                        : request.getParameter("questionnaireId");
        if (StringUtils.isEmpty(questionnaireId)) {
          questionnaireId =
                  (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
        }
        if (StringUtils.isNotEmpty(questionnaireId)) {
          request.getSession().removeAttribute(sessionStudyCount + "actionType");
          questionnaireBo =
                  studyQuestionnaireService.getQuestionnaireById(
                          Integer.valueOf(questionnaireId), studyBo.getCustomStudyId());
          map.addAttribute("questionnaireBo", questionnaireBo);
        }
        String actionType =
                FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionType"))
                        ? ""
                        : request.getParameter("actionType");
        if (StringUtils.isEmpty(actionType)) {
          actionType = (String) request.getSession().getAttribute(sessionStudyCount + "actionType");
        }
        String id = "";
        String checkRefreshFlag = "";
        if (!"add".equals(actionType)) {
          if (session != null
                  && session.getAttribute("gId") != null && request.getParameter("id") == null) {
            id = (String) session.getAttribute("gId");
            checkRefreshFlag = (String) session.getAttribute("flag");
          } else {
            id =
                    FdahpStudyDesignerUtil.isEmpty(request.getParameter("id"))
                            ? ""
                            : request.getParameter("id");
            checkRefreshFlag =
                    FdahpStudyDesignerUtil.isEmpty(request.getParameter("checkRefreshFlag"))
                            ? ""
                            : request.getParameter("checkRefreshFlag");

            session.setAttribute("gId", id);
            session.setAttribute("flag", checkRefreshFlag);
          }
        }
        String actionOn =
                FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionOn"))
                        ? ""
                        : request.getParameter("actionOn");
        if (StringUtils.isEmpty(actionOn)) {
          actionOn = (String) request.getSession().getAttribute(sessionStudyCount + "actionOn");
        }
        map.addAttribute("actionOn", actionOn);

        //getting the List<step id's> based on the id from group_mapping table
        if (!"".equals(id)) {
          groupStepLists = studyQuestionnaireService.getStepId(id, questionnaireId);
        }
        map.addAttribute("groupStepLists", groupStepLists);
        GroupMappingBo lastStepObject = null;
        if (groupStepLists.size() != 0) {
          lastStepObject = groupStepLists.get(groupStepLists.size() - 1);
        }
        Integer questionStepId = null;
        String stepType = null;
        Integer stepId = null;
        Integer lastQuestinObjectValue = null;
        if (lastStepObject != null) {
          questionStepId = lastStepObject.getQuestionnaireStepId();
          stepId = Integer.valueOf(lastStepObject.getStepId());
          //getting stepType by sending questionStepId
          stepType = studyQuestionnaireService.getStepType(questionStepId, stepId);
          System.out.println(stepType);
        }
        map.addAttribute("stepType", stepType);
        String selectionStyle = null;
        if (stepType != null && questionStepId != null) {
          if (stepType.equals("Question")) {
            responseType = studyQuestionnaireService.getResponseType(questionStepId);
            if (responseType.equals(6)) {
              selectionStyle = studyQuestionnaireService.getTextChoiceSelectionStyle(stepId);
            }
            operatorsList = getOperatorsListByResponseType(responseType);
          } else if (stepType.equals("Form")) {
            questionIdList = studyQuestionnaireService.getQuestionIdList(questionStepId);
            map.addAttribute("questionIdList", questionIdList);
            if (!questionIdList.isEmpty()) {
              lastQuestinObjectValue = questionIdList.get(0);
              System.out.println(lastQuestinObjectValue);
              responseType = studyQuestionnaireService.getResponseTypeForFormStep(lastQuestinObjectValue);
              if (responseType.equals(6)) {
                selectionStyle = studyQuestionnaireService.getTextChoiceSelectionStyle(lastQuestinObjectValue);
              }
              operatorsList = getOperatorsListByResponseType(responseType);
            }
          }
          map.addAttribute("operators", operatorsList);
          map.addAttribute("responseType", responseType);
        }
        map.addAttribute("selectionStyle", selectionStyle);

        if (StringUtils.isNotEmpty(questionnaireId)) {
          groupsList = studyQuestionnaireService.getGroupsListForGroupsPage(questionnaireId, StringUtils.isNotBlank(id) ? Integer.parseInt(id) : 0);
          map.addAttribute("groupsList", groupsList);
        }

        if (!"".equalsIgnoreCase(checkRefreshFlag)) {
          if (!"".equals(id)) {
            grpId = Integer.parseInt(id);
            actionPage = FdahpStudyDesignerConstants.EDIT_PAGE;
            request.getSession().removeAttribute(sessionStudyCount + "actionType");
            groupsBo = studyQuestionnaireService.getGroupsDetails(grpId);
            preLoadLogicBoList = studyQuestionnaireService.getPreLoadLogicDetails(StringUtils.isNotEmpty(id) ? Integer.parseInt(id) : null);
          } else {
            request.getSession().removeAttribute(sessionStudyCount + "actionType");
            actionPage = FdahpStudyDesignerConstants.ADD_PAGE;
          }
          if ("edit".equals(actionType) || "add".equals(actionType) || !"".equals(id)) {
            map.addAttribute("actionType", "edit");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "edit");
          } else {
            map.addAttribute("actionType", "view");
            request.getSession().setAttribute(sessionStudyCount + "actionType", "view");
          }
        }
        if (StringUtils.isNotEmpty(questionnaireId)) {
          List<QuestionnairesStepsBo> stepsBoList = studyQuestionnaireService
                  .getStepsForGroups(Integer.parseInt(questionnaireId), StringUtils.isNotBlank(id) ? Integer.parseInt(id) : 0);
          map.addAttribute("qTreeMap", stepsBoList);
        }
        map.addAttribute("actionPage", actionPage);
        map.addAttribute("studyBo", studyBo);
        map.addAttribute("groupsBo", groupsBo);
        map.addAttribute("preLoadLogicBoList", preLoadLogicBoList);
        map.addAttribute("_S", sessionStudyCount);
        map.addAttribute("isAutoSaved", request.getParameter("isAutoSaved"));
        mav = new ModelAndView("addOrEditGroupsPage", map);
      } else {
        mav = new ModelAndView("redirect:/adminStudies/viewGroups.do", map);
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - addOrEditGroupsDetails() - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - addOrEditGroupsDetails() - Ends");
    return mav;
  }

  @RequestMapping("/adminStudies/addOrUpdateGroupsDetails.do")
  public ModelAndView addOrUpdateGroupsDetails(
          HttpServletRequest request, GroupsBean groupsBean) {
    logger.info("UsersController - addOrUpdateGroupsDetails() - Starts");
    ModelAndView mav = new ModelAndView();
    ModelMap map = new ModelMap();
    String msg = FdahpStudyDesignerConstants.FAILURE;
    boolean addFlag = false;
    StudyBo studyBo = null;
    List<GroupMappingBo> groupStepLists = new ArrayList<>();
    QuestionnaireBo questionnaireBo = null;
    List<Integer> questionIdList = new ArrayList<>();
    List<String> operatorsList = new ArrayList<>();
    Integer responseType = null;
    List<GroupsBo> groupsList = null;
    List<PreLoadLogicBo> preLoadLogicBoList = null;
    Map<String, String> propMap = FdahpStudyDesignerUtil.getAppProperties();
    try {
      HttpSession session = request.getSession();
      SessionObject userSession =
              (SessionObject) session.getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
              StringUtils.isNumeric(request.getParameter("_S"))
                      ? Integer.parseInt(request.getParameter("_S"))
                      : 0;
      if ((userSession != null)
              && (userSession.getStudySession() != null)
              && userSession.getStudySession().contains(sessionStudyCount)) {

      String studyId =
              (String)
                      request
                              .getSession()
                              .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
        if (StringUtils.isEmpty(studyId)) {
          studyId =
                  FdahpStudyDesignerUtil.isEmpty(
                          request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                          == true
                          ? "0"
                          : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
        }
        if (StringUtils.isNotEmpty(studyId)) {
          studyBo = studyService.getStudyById(studyId, userSession.getUserId());
          map.addAttribute(FdahpStudyDesignerConstants.STUDY_BO, studyBo);
        }
        String language = request.getParameter("language");
        if (FdahpStudyDesignerUtil.isNotEmpty(language)
                && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
          this.setStudyLangData(studyId, language, map);
        }
        map.addAttribute("currLanguage", language);
        String languages = studyBo.getSelectedLanguages();
        List<String> langList = new ArrayList<>();
        Map<String, String> langMap = new HashMap<>();
        if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
          langList = Arrays.asList(languages.split(","));
          for (String string : langList) {
            langMap.put(string, MultiLanguageCodes.getValue(string));
          }
        }
        map.addAttribute("languageList", langMap);

        String questionnaireId = (String) request.getSession().getAttribute(sessionStudyCount +"questionnaireId");
        if (StringUtils.isNotEmpty(questionnaireId)) {
          request.getSession().removeAttribute(sessionStudyCount + "actionType");
          questionnaireBo =
                  studyQuestionnaireService.getQuestionnaireById(
                          Integer.valueOf(questionnaireId), studyBo.getCustomStudyId());
          map.addAttribute("questionnaireBo", questionnaireBo);
        }
                  String id =
             FdahpStudyDesignerUtil.isEmpty(request.getParameter("id"))
                     ? ""
                     : request.getParameter("id");
        if (StringUtils.isNotEmpty(questionnaireId)) {
          if (StringUtils.isNotEmpty(questionnaireId)) {
            groupsList = studyQuestionnaireService.getGroupsListForGroupsPage(questionnaireId, StringUtils.isNotBlank(id) ? Integer.parseInt(id) : 0);
            map.addAttribute("groupsList", groupsList);
          }
        }
       
     if (StringUtils.isEmpty(id)) {
    	 id =
             (String) request.getSession().getAttribute(sessionStudyCount + "id");
         request.getSession().setAttribute(sessionStudyCount + "id", id);
       }
     String buttonText =
             FdahpStudyDesignerUtil.isEmpty(
                     request.getParameter(FdahpStudyDesignerConstants.BUTTON_TEXT))
                 ? ""
                 : request.getParameter(FdahpStudyDesignerConstants.BUTTON_TEXT);
         if (!("").equals(buttonText)) {
           if (("save").equalsIgnoreCase(buttonText)) {
        	   groupsBean.setAction(false);
           } else if (("done").equalsIgnoreCase(buttonText)) {
        	   groupsBean.setAction(true);
           }
         }
         String actionType =
                 FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionType"))
                         ? ""
                         : request.getParameter("actionType");
         if (StringUtils.isEmpty(actionType)) {
           actionType = (String) request.getSession().getAttribute(sessionStudyCount + "actionType");
         }
         Integer isPublished =
        		 StringUtils.isNumeric(request.getParameter("isPublished"))
                 ? Integer.parseInt(request.getParameter("isPublished"))
                 : 0;
         groupsBean.setIsPublished(isPublished);
         groupsBean.setQuestionnaireId(Integer.parseInt(questionnaireId));
         groupsBean.setStudyId(Integer.parseInt(studyId));
         msg = studyQuestionnaireService.addOrUpdateGroupsDetails(groupsBean, userSession);
        preLoadLogicBoList = studyQuestionnaireService.getPreLoadLogicDetails(StringUtils.isNotEmpty(id) ? Integer.parseInt(id) : null);

        if (!msg.equalsIgnoreCase(FdahpStudyDesignerConstants.FAILURE) ) {
            if ((groupsBean != null) && (groupsBean.getId() == null)) {
              if (("save").equalsIgnoreCase(buttonText)) {
                map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, "Content saved as draft");
              } else {
            request
                    .getSession()
                    .setAttribute(
                         sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG, "Group successfully added.");
          }
            
        } else {
          if (("save").equalsIgnoreCase(buttonText)) {
            map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, "Content saved as draft");
          } else {
            request
                .getSession()
                .setAttribute(
                    sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG,
                    "Group successfully updated.");
          }
        }
      } else {
        if ((groupsBean != null) && (groupsBean.getId() == null)) {
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG,
                  "Failed to add group.");
        } else {
          request
              .getSession()
              .setAttribute(
                  sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG,
                  "Failed to update group.");
        }
        
      }
        //getting the List<step id's> based on the id from group_mapping table
        if (!"".equals(id)) {
          groupStepLists = studyQuestionnaireService.getStepId(id, questionnaireId);
        }
        map.addAttribute("groupStepLists", groupStepLists);
        GroupMappingBo lastStepObject = null;
        if(groupStepLists.size() != 0) {
          lastStepObject = groupStepLists.get(groupStepLists.size() - 1);
        }
        Integer questionStepId = null;
        String stepType = null;
        Integer stepId = null;
        Integer lastQuestinObjectValue = null;
        if(lastStepObject !=null) {
          questionStepId = lastStepObject.getQuestionnaireStepId();
          stepId = Integer.valueOf(lastStepObject.getStepId());
          //getting stepType by sending questionStepId
          stepType = studyQuestionnaireService.getStepType(questionStepId, stepId);
        }
        map.addAttribute("stepType", stepType);
        String selectionStyle = null;
        if(stepType != null && questionStepId != null){
          if(stepType.equals("Question")){
            responseType = studyQuestionnaireService.getResponseType(questionStepId);
            if(responseType.equals(6)) {
              selectionStyle = studyQuestionnaireService.getTextChoiceSelectionStyle(stepId);
            }
            operatorsList = getOperatorsListByResponseType(responseType);
          }
          else if(stepType.equals("Form")){
            questionIdList = studyQuestionnaireService.getQuestionIdList(questionStepId);
            map.addAttribute("questionIdList", questionIdList);
            if(!questionIdList.isEmpty()) {
              lastQuestinObjectValue = questionIdList.get(0);
              System.out.println(lastQuestinObjectValue);
              responseType = studyQuestionnaireService.getResponseTypeForFormStep(lastQuestinObjectValue);
              if(responseType.equals(6)) {
                selectionStyle = studyQuestionnaireService.getTextChoiceSelectionStyle(lastQuestinObjectValue);
              }
              operatorsList = getOperatorsListByResponseType(responseType);
            }
          }
          map.addAttribute("operators",operatorsList);
          map.addAttribute("responseType",responseType);
        }
        map.addAttribute("selectionStyle", selectionStyle);
        List<QuestionnairesStepsBo> stepsBoList = studyQuestionnaireService
                .getStepsForGroups(Integer.parseInt(questionnaireId), StringUtils.isNotBlank(id) ? Integer.parseInt(id) : 0);
        map.addAttribute("qTreeMap", stepsBoList);
        map.addAttribute("actionType", actionType);
        map.addAttribute("_S", sessionStudyCount);
        map.addAttribute("preLoadLogicBoList", preLoadLogicBoList);
        map.addAttribute("groupsBo", groupsBean);
        map.addAttribute("isAutoSaved", request.getParameter("isAutoSaved"));
        if (("save").equalsIgnoreCase(buttonText)) {
          mav = new ModelAndView("addOrEditGroupsPage", map);
        } else {
          mav = new ModelAndView("redirect:viewGroups.do", map);
        }
      }
      
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - addOrUpdateGroupsDetails() - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - addOrUpdateGroupsDetails() - Ends");
    return mav;
  }
  
  @RequestMapping(value = "/adminStudies/deleteGroup.do", method = RequestMethod.POST)
  public void deleteGroup(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - deleteGroup - Starts");
    JSONObject jsonobject = new JSONObject();
    PrintWriter out = null;
    String message = FdahpStudyDesignerConstants.FAILURE;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String questionnaireId = (String) request
                .getSession()
                .getAttribute(sessionStudyCount +"questionnaireId");
        String id =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("id"))
                ? ""
                : request.getParameter("id");
        if (!id.isEmpty()) {

          //getting the List<step id's> based on the id from group_mapping table
          List<GroupMappingBo> groupMappingBo = studyQuestionnaireService.getStepId(id,questionnaireId);
          //disabling the flag based on the step id in questionnaires_steps table
          String msg = studyQuestionnaireService.groupFlagDisable(groupMappingBo,questionnaireId);
          // Delete all the steps in mapping table based on group id
          String msgg = studyQuestionnaireService.deleteGroupMaprecords(id);
          //Delete group from the table

          message =
              studyQuestionnaireService.deleteGroup(
                  id,sesObj);
        }
      }
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - deleteGroup - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - deleteGroup - Ends");
  }

  
  @RequestMapping(
	      value = "/adminStudies/validateGroupIdKey.do",
	      method = RequestMethod.POST)
	  public void validateGroupId(
	      HttpServletRequest request, HttpServletResponse response) {
	    logger.info("StudyQuestionnaireController - validateGroupId - Starts");
	    String message = FdahpStudyDesignerConstants.FAILURE;
	    JSONObject jsonobject = new JSONObject();
	    PrintWriter out = null;
	    try {
	        SessionObject sesObj =
	            (SessionObject)
	                request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
	        Integer sessionStudyCount =
	            StringUtils.isNumeric(request.getParameter("_S"))
	                ? Integer.parseInt(request.getParameter("_S"))
	                : 0;
	        if ((sesObj != null)
	            && (sesObj.getStudySession() != null)
	            && sesObj.getStudySession().contains(sessionStudyCount)) {
	        	String  questionnaireId =
	                    FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
	                      ? ""
	                      : request.getParameter("questionnaireId");
	           if (StringUtils.isEmpty(questionnaireId)) {
	        	   questionnaireId =
	                   (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
	               request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
	             }
	          String groupId =
	              FdahpStudyDesignerUtil.isEmpty(request.getParameter("groupId"))
	                  ? ""
	                  : request.getParameter("groupId");
	          String studyId =
	                  (String)
	                      request
	                          .getSession()
	                          .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
	              if (StringUtils.isEmpty(studyId)) {
	            	  studyId =
	                    FdahpStudyDesignerUtil.isEmpty(
	                            request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
	                        ? ""
	                        : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
	              }
	              if (!questionnaireId.isEmpty() && !groupId.isEmpty()) {
	                  message =
	                      studyQuestionnaireService.checkGroupId(
	                          questionnaireId,
	                          groupId,
	                          studyId);
	                }
	          
	          
	        }
	        jsonobject.put("message", message);
	        response.setContentType("application/json");
	        out = response.getWriter();
	        out.print(jsonobject);
	      } catch (Exception e) {
	        logger.error("StudyQuestionnaireController - validateGroupId - ERROR", e);
	      }
	      logger.info("StudyQuestionnaireController - validateGroupId - Ends");
  }
  
  @RequestMapping(
	      value = "/adminStudies/validateGroupName.do",
	      method = RequestMethod.POST)
	  public void validateGroupName(
	      HttpServletRequest request, HttpServletResponse response) {
	    logger.info("StudyQuestionnaireController - validateGroupName - Starts");
	    String message = FdahpStudyDesignerConstants.FAILURE;
	    JSONObject jsonobject = new JSONObject();
	    PrintWriter out = null;
	    try {
	        SessionObject sesObj =
	            (SessionObject)
	                request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
	        Integer sessionStudyCount =
	            StringUtils.isNumeric(request.getParameter("_S"))
	                ? Integer.parseInt(request.getParameter("_S"))
	                : 0;
	        if ((sesObj != null)
	            && (sesObj.getStudySession() != null)
	            && sesObj.getStudySession().contains(sessionStudyCount)) {
	        	String  questionnaireId =
	                    FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
	                      ? ""
	                      : request.getParameter("questionnaireId");
	           if (StringUtils.isEmpty(questionnaireId)) {
	        	   questionnaireId =
	                   (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
	               request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
	             }
	          String groupName =
	              FdahpStudyDesignerUtil.isEmpty(request.getParameter("groupName"))
	                  ? ""
	                  : request.getParameter("groupName");
	          String studyId =
	                  (String)
	                      request
	                          .getSession()
	                          .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);
	              if (StringUtils.isEmpty(studyId)) {
	            	  studyId =
	                    FdahpStudyDesignerUtil.isEmpty(
	                            request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
	                        ? ""
	                        : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
	              }
	              if (!questionnaireId.isEmpty() && !groupName.isEmpty()) {
	                  message =
	                      studyQuestionnaireService.checkGroupName(
	                          questionnaireId,
	                          groupName,
	                          studyId);
	                }
	          
	          
	        }
	        jsonobject.put("message", message);
	        response.setContentType("application/json");
	        out = response.getWriter();
	        out.print(jsonobject);
	      } catch (Exception e) {
	        logger.error("StudyQuestionnaireController - validateGroupId - ERROR", e);
	      }
	      logger.info("StudyQuestionnaireController - validateGroupId - Ends");
  }

  @RequestMapping(value = "/adminStudies/assignGroup.do", method = RequestMethod.POST)
  public void assignGroup(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - assignGroup - Starts");
    String status = FdahpStudyDesignerConstants.FAILURE;
    List<GroupMappingBo> groupMappingBo =null;
    JSONObject jsonobject = new JSONObject();
    ObjectMapper mapper = new ObjectMapper();
    PrintWriter out = null;
    StudyBo studyBo = null;
    String message = "";
    String questionnaireId = "";
    try {
      SessionObject sesObj =
              (SessionObject)
                      request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
              StringUtils.isNumeric(request.getParameter("_S"))
                      ? Integer.parseInt(request.getParameter("_S"))
                      : 0;
      if (sesObj != null
              && sesObj.getStudySession() != null
              && sesObj.getStudySession().contains(sessionStudyCount)) {

        String actionType =
                FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionType"))
                        ? ""
                        : request.getParameter("actionType");
        if (StringUtils.isEmpty(actionType)) {
          actionType = (String) request.getSession().getAttribute(sessionStudyCount + "actionType");
        }
        Integer grpId =
                Integer.valueOf(FdahpStudyDesignerUtil.isEmpty(request.getParameter("grpId"))
                        ? ""
                        : request.getParameter("grpId"));

        Integer count =
                Integer.valueOf(FdahpStudyDesignerUtil.isEmpty(request.getParameter("count"))
                        ? ""
                        : request.getParameter("count"));

        System.out.println(request.getParameter("steparray"));
        String stepArray =FdahpStudyDesignerUtil.isEmpty(request.getParameter("steparray"))
                ? ""
                : request.getParameter("steparray");

        List<String> arr = Arrays.asList(stepArray.replaceAll("[\\[\\]]", "").split(","));
        String studyId =
                (String)
                        request
                                .getSession()
                                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);

        if (StringUtils.isEmpty(studyId)) {
          studyId =
                  FdahpStudyDesignerUtil.isEmpty(
                          request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                          == true
                          ? "0"
                          : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
        }

        questionnaireId =
                FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                        ? ""
                        : request.getParameter("questionnaireId");
        if (StringUtils.isEmpty(questionnaireId)) {
          questionnaireId =
                  (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
          request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
        }

        if (StringUtils.isNotEmpty(studyId)) {
          request.getSession().removeAttribute(sessionStudyCount + "actionType");
          studyBo = studyService.getStudyById(studyId, sesObj.getUserId());

        }
        if (!String.valueOf(grpId).isEmpty()) {
          groupMappingBo = studyQuestionnaireService.getStepId(String.valueOf(grpId), questionnaireId);
        }
        if(!groupMappingBo.isEmpty()) {
          if (StringUtils.isNotEmpty(studyId)) {
            groupMappingBo =
                    studyQuestionnaireService.assignQuestionSteps(arr, grpId, questionnaireId);
            status = FdahpStudyDesignerConstants.SUCCESS;
            message = "Group has been assigned successfully";
          }
        }else{
          if(count>=2){
            groupMappingBo =
                    studyQuestionnaireService.assignQuestionSteps(arr, grpId, questionnaireId);
            status = FdahpStudyDesignerConstants.SUCCESS;
            message = "Group has been assigned successfully";
          }else{
            message = "A group should consist of more than one steps. Request you to select multiple steps for assigning to the group.";
          }
        }

        if ("edit".equals(actionType)) {
          jsonobject.put("actionType", "edit");
          request.getSession().setAttribute(sessionStudyCount + "actionType", "edit");
        } else {
          jsonobject.put("actionType", "view");
          request.getSession().setAttribute(sessionStudyCount + "actionType", "view");
        }
      }
      jsonobject.put("message", message);
      jsonobject.put("status", status);
      jsonobject.put("groupMappingBo", new JSONArray(mapper.writeValueAsString(groupMappingBo)));

      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);

    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - assignGroup - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - assignGroup - Ends");
  }
  
  @RequestMapping(value = "/adminStudies/deassignGroup.do")
  public ModelAndView deassignGroup(HttpServletRequest request) {
  logger.info("StudyQuestionnaireController - deassignGroup() - Starts");
  ModelAndView mav = new ModelAndView();
  ModelMap map = new ModelMap();
  GroupsBo groupsBo = null;
  String questionnaireId = "";
  String sucMsg = "";
  String errMsg = "";
  String actionPage = "";
  StudyBo studyBo=null;
  List<GroupMappingStepBean> groupMappingBeans=new ArrayList<GroupMappingStepBean>();
  try {
    SessionObject sesObj =
            (SessionObject)
                    request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
    Integer sessionStudyCount =
            StringUtils.isNumeric(request.getParameter("_S"))
                    ? Integer.parseInt(request.getParameter("_S"))
                    : 0;
    if (sesObj != null
            && sesObj.getStudySession() != null
            && sesObj.getStudySession().contains(sessionStudyCount)) {

      if (null
              != request
              .getSession()
              .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG)) {
        sucMsg =
                (String)
                        request
                                .getSession()
                                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
        map.addAttribute(FdahpStudyDesignerConstants.SUC_MSG, sucMsg);
        request
                .getSession()
                .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.SUC_MSG);
      }
      if (null
              != request
              .getSession()
              .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG)) {
        errMsg =
                (String)
                        request
                                .getSession()
                                .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
        map.addAttribute(FdahpStudyDesignerConstants.ERR_MSG, errMsg);
        request
                .getSession()
                .removeAttribute(sessionStudyCount + FdahpStudyDesignerConstants.ERR_MSG);
      }
      String actionType =
              FdahpStudyDesignerUtil.isEmpty(request.getParameter("actionType"))
                      ? ""
                      : request.getParameter("actionType");
      if (StringUtils.isEmpty(actionType)) {
        actionType = (String) request.getSession().getAttribute(sessionStudyCount + "actionType");
      }

      String studyId =
              (String)
                      request
                              .getSession()
                              .getAttribute(sessionStudyCount + FdahpStudyDesignerConstants.STUDY_ID);

      if (StringUtils.isEmpty(studyId)) {
        studyId =
                FdahpStudyDesignerUtil.isEmpty(
                        request.getParameter(FdahpStudyDesignerConstants.STUDY_ID))
                        == true
                        ? "0"
                        : request.getParameter(FdahpStudyDesignerConstants.STUDY_ID);
      }
      if (StringUtils.isNotEmpty(studyId)) {
        request.getSession().removeAttribute(sessionStudyCount + "actionType");
        studyBo = studyService.getStudyById(studyId, sesObj.getUserId());
        map.addAttribute("studyBo", studyBo);
      }
      if (studyBo == null) {
        studyBo = new StudyBo();
        studyBo.setType(FdahpStudyDesignerConstants.STUDY_TYPE_GT);
        studyBo.setStudyLanguage(FdahpStudyDesignerConstants.STUDY_LANGUAGE_ENGLISH);
      } else if ((studyBo != null) && StringUtils.isNotEmpty(studyBo.getCustomStudyId())) {
        request
                .getSession()
                .setAttribute(
                        sessionStudyCount + FdahpStudyDesignerConstants.CUSTOM_STUDY_ID,
                        studyBo.getCustomStudyId());
      }

      String language = request.getParameter("language");
      if (FdahpStudyDesignerUtil.isNotEmpty(language)
              && !MultiLanguageCodes.ENGLISH.getKey().equals(language)) {
        this.setStudyLangData(studyId, language, map);
      }
      map.addAttribute("currLanguage", language);
      String languages = studyBo.getSelectedLanguages();
      List<String> langList = new ArrayList<>();
      Map<String, String> langMap = new HashMap<>();
      if (FdahpStudyDesignerUtil.isNotEmpty(languages)) {
        langList = Arrays.asList(languages.split(","));
        for (String string : langList) {
          langMap.put(string, MultiLanguageCodes.getValue(string));
        }
      }
      map.addAttribute("languageList", langMap);
      if ("edit".equals(actionType)) {
        map.addAttribute("actionType", "edit");
        request.getSession().setAttribute(sessionStudyCount + "actionType", "edit");
      } else {
        map.addAttribute("actionType", "view");
        request.getSession().setAttribute(sessionStudyCount + "actionType", "view");
      }
      map.addAttribute("actionPage", actionPage);
      questionnaireId =
              FdahpStudyDesignerUtil.isEmpty(request.getParameter("questionnaireId"))
                      ? ""
                      : request.getParameter("questionnaireId");
      if (StringUtils.isEmpty(questionnaireId)) {
        questionnaireId =
                (String) request.getSession().getAttribute(sessionStudyCount + "questionnaireId");
        request.getSession().setAttribute(sessionStudyCount + "questionnaireId", questionnaireId);
      }
      String grpId =
              FdahpStudyDesignerUtil.isEmpty(request.getParameter("id"))
                      ? ""
                      : request.getParameter("id");
      if (StringUtils.isEmpty(grpId)) {
    	  grpId =
                  (String) request.getSession().getAttribute(sessionStudyCount + "id");
          request.getSession().setAttribute(sessionStudyCount + "id", grpId);
        }

        groupMappingBeans =
                studyQuestionnaireService.getGroupsAssignedList(
                    Integer.valueOf(grpId));
      map.addAttribute("groupsAssignedList", groupMappingBeans);
        map.addAttribute("grpId", grpId);
        map.addAttribute("_S", sessionStudyCount);
        mav = new ModelAndView("deAssignGroup", map);
      } 

    } catch(Exception e){
      logger.error("StudyQuestionnaireController - deassignGroup() - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - deassignGroup() - Ends");
    return mav;
}
  
  @RequestMapping(value = "/adminStudies/deassignSteps.do", method = RequestMethod.POST)
  public void deassignSteps(HttpServletRequest request, HttpServletResponse response) {
    logger.info("StudyQuestionnaireController - deleteGroup - Starts");
    ModelAndView mav = new ModelAndView();
    ModelMap map = new ModelMap();
    JSONObject jsonobject = new JSONObject();
    ObjectMapper mapper = new ObjectMapper();
    PrintWriter out = null;
    String message = FdahpStudyDesignerConstants.FAILURE;
    try {
      SessionObject sesObj =
          (SessionObject)
              request.getSession().getAttribute(FdahpStudyDesignerConstants.SESSION_OBJECT);
      Integer sessionStudyCount =
          StringUtils.isNumeric(request.getParameter("_S"))
              ? Integer.parseInt(request.getParameter("_S"))
              : 0;
      if (sesObj != null
          && sesObj.getStudySession() != null
          && sesObj.getStudySession().contains(sessionStudyCount)) {
        String questionnaireId = (String) request
                .getSession()
                .getAttribute(sessionStudyCount +"questionnaireId");
        String id =
            FdahpStudyDesignerUtil.isEmpty(request.getParameter("stepId"))
                ? ""
                : request.getParameter("stepId");
        if (!id.isEmpty()) {

          //getting the List<step id's> based on the id from group_mapping table
          GroupMappingBo groupMappingBo = studyQuestionnaireService.getStepDetails(id,questionnaireId);

          //disabling the flag based on the step id in questionnaires_steps table
          String msg = studyQuestionnaireService.stepFlagDisable(groupMappingBo,questionnaireId);

          // Delete all the steps in mapping table based on step id
          String msgg = studyQuestionnaireService.deleteStepMaprecords(id,questionnaireId);
          if(msg.equalsIgnoreCase("success") && msgg.equalsIgnoreCase("success"))
          {
        	  message=FdahpStudyDesignerConstants.SUCCESS;
          }

        }
        
      }
		/*
		 * map.addAttribute("message", message); mav = new
		 * ModelAndView("redirect:/adminStudies/deassignGroup.do", map);
		 */
      jsonobject.put("message", message);
      response.setContentType("application/json");
      out = response.getWriter();
      out.print(jsonobject);
     
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - deleteGroup - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - deleteGroup - Ends");
  }



  @RequestMapping("/adminStudies/refreshSourceKeys.do")
  public void refreshSourceKeys(HttpServletResponse response, StepDropdownBean bean)
          throws IOException {
    logger.info("StudyQuestionnaireController - refreshSourceKeys() - Starts");
    String msg = FdahpStudyDesignerConstants.FAILURE;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out;
    try {
      Integer seqNo = bean.getSeqNo();
      String questionnaireId = bean.getQuestionnaireId();
      String caller = bean.getCaller();
      String PRE_LOAD = "preload";
      if ((PRE_LOAD.equals(bean.getCaller()) && seqNo == null && bean.getIsDifferentSurveyPreload()) ||
              (!PRE_LOAD.equals(bean.getCaller()) && seqNo == null) ||
              (PRE_LOAD.equals(caller) && (bean.getIsDifferentSurveyPreload() != null && bean.getIsDifferentSurveyPreload())) ||
              (!PRE_LOAD.equals(caller) && (bean.getIsDifferentSurveyPiping() != null && bean.getIsDifferentSurveyPiping()))) {
        seqNo = -1;
      }
      if (seqNo != null && StringUtils.isNotBlank(questionnaireId)) {
        List<QuestionnairesStepsBo> sourceKeys = studyQuestionnaireService.getSameSurveySourceKeys(
                Integer.parseInt(questionnaireId), seqNo, bean.getCaller(), bean.getStepId(),
                StringUtils.isNotBlank(bean.getPreLoadQuestionnaireId()) ? Integer.parseInt(bean.getPreLoadQuestionnaireId()) : null);

        List<GroupsBo> groupsList = studyQuestionnaireService.getGroupsForPreloadAndPostLoad(
                bean.getPreLoadQuestionnaireId(), questionnaireId, bean.getInstructionFormId(), true);
        jsonobject.put("sourceKeys", new JSONArray(new Gson().toJson(sourceKeys)));
        jsonobject.put("groupList", new JSONArray(new Gson().toJson(groupsList)));
      }
      msg = FdahpStudyDesignerConstants.SUCCESS;
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - refreshSourceKeys() - ERROR", e);
    }
    logger.info("StudyQuestionnaireController - refreshSourceKeys() - Ends");
    jsonobject.put("message", msg);
    response.setContentType("application/json");
    out = response.getWriter();
    out.print(jsonobject);
  }

  @RequestMapping(value = "/adminStudies/submitPiping.do", method = RequestMethod.POST)
  public void submitPiping(
          HttpServletRequest request,
          HttpServletResponse response) throws IOException {
    logger.info("StudyQuestionnaireController - submitPiping() - Starts");
    String status = FdahpStudyDesignerConstants.FAILURE;
    String message;
    JSONObject jsonobject = new JSONObject();
    PrintWriter out;
    try {
      String pipingData = request.getParameter("dataObject");
      if (StringUtils.isNotBlank(pipingData)) {
        QuestionnaireStepBean questionnaireStepBean = new ObjectMapper().readValue(pipingData, QuestionnaireStepBean.class);
        if (questionnaireStepBean != null && questionnaireStepBean.getStepId() != null) {
          studyQuestionnaireService.saveOrUpdatePipingData(questionnaireStepBean);
          status = FdahpStudyDesignerConstants.SUCCESS;
          message = "Piping details saved successfully.";
        } else {
          message = "Invalid request data";
        }
      } else {
        message = "Invalid request data";
      }
    } catch (Exception e) {
      logger.error("StudyQuestionnaireController - submitPiping() - ERROR", e);
      message = "Error while saving piping data.";
    }
    logger.info("StudyQuestionnaireController - submitPiping() - Ends");
    jsonobject.put("status", status);
    jsonobject.put("message", message);
    response.setContentType("application/json");
    out = response.getWriter();
    out.print(jsonobject);
  }
}

