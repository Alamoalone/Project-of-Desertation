
package com.rclgroup.dolphin.web.igm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.niit.control.common.exception.BusinessException;
import com.niit.control.common.exception.ExceptionFactory;
import com.niit.control.dao.AncestorJdbcDao;
import com.niit.control.dao.JdbcRowMapper;
import com.niit.control.dao.JdbcStoredProcedure;
import com.rclgroup.dolphin.web.igm.vo.CFSCustomCode;
import com.rclgroup.dolphin.web.igm.vo.Consignee;
import com.rclgroup.dolphin.web.igm.vo.Consigner;
import com.rclgroup.dolphin.web.igm.vo.ContainerDetails;
import com.rclgroup.dolphin.web.igm.vo.DropDownMod;
import com.rclgroup.dolphin.web.igm.vo.ImportGeneralManifestMod;
import com.rclgroup.dolphin.web.igm.vo.MarksNumber;
import com.rclgroup.dolphin.web.igm.vo.NotifyParty;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
/**
 * The Class ImportGeneralManifestDaoImpl.
 */
public class ImportGeneralManifestDaoImpl extends AncestorJdbcDao implements ImportGeneralManifestDao {

	Set<String> blrNumber = null;
	
	Set<String> PodTerminal = null;

	/*qyery for get refresh data*/
	private static final String CONSIGNEE_QUERY =  "SELECT  FK_BL_NO ,FK_CUSTOMER_CODE ,CUSTOMER_NAME ,ADDRESS_LINE_1,ADDRESS_LINE_2,ADDRESS_LINE_3,ADDRESS_LINE_4,CITY,STATE,ZIP,DN_COUNTRY_CODE  FROM RCLTBLS.DEX_BL_CUSTOMERS " + 
			" WHERE CUSTOMER_TYPE='C'AND FK_BL_NO in ";
	
	private static final String CONSIGNER_QUERY =  "SELECT  FK_BL_NO ,FK_CUSTOMER_CODE ,CUSTOMER_NAME ,ADDRESS_LINE_1,ADDRESS_LINE_2,ADDRESS_LINE_3,ADDRESS_LINE_4,CITY,STATE,ZIP,DN_COUNTRY_CODE  FROM RCLTBLS.DEX_BL_CUSTOMERS " + 
			" WHERE CUSTOMER_TYPE ='S' AND FK_BL_NO in ";

	private static final String CONTAINER_DETAILS_QUERY = "SELECT RCLTBLS.DEX_BL_CONTAINERS.FK_BL_NO,DN_CONTAINER_NO, RCLTBLS.DEX_BL_CONTAINERS.CARRIER_SEAL ," + 
			"            RCLTBLS.DEX_BL_CONTAINERS.EQUIPMENT_STATUS,RCLTBLS.DEX_BL_CONTAINERS.QTY_PACKAGES,         " + 
			"            RCLTBLS.DEX_BL_CONTAINERS.METRIC_WEIGHT,IDP055.EYEQSZ AS CONTAINERSIZE,IDP055.EYEQTP  AS CONTAINERTYPE, null as EQUIPMENT_LOAD_STATUS,null as SOC_FLAG,null as EQUIPMENT_SEAL_TYPE,null as PARAM_VALUE " + 
			"			 FROM RCLTBLS.DEX_BL_CONTAINERS INNER JOIN IDP055 ON IDP055.EYBLNO=RCLTBLS.DEX_BL_CONTAINERS.FK_BL_NO " + 
			"            AND IDP055.EYEQNO=RCLTBLS.DEX_BL_CONTAINERS.DN_CONTAINER_NO  " + 
			"			 WHERE  RCLTBLS.DEX_BL_CONTAINERS.FK_BL_NO IN ";

	private static final String NOTIFY_PARTY_QUERY = "SELECT FK_BL_NO, FK_CUSTOMER_CODE, CUSTOMER_NAME, ADDRESS_LINE_1, ADDRESS_LINE_2, ADDRESS_LINE_3, ADDRESS_LINE_4, CITY, STATE, DN_COUNTRY_CODE, ZIP FROM RCLTBLS.DEX_BL_CUSTOMERS WHERE CUSTOMER_TYPE IN('N','1','2','3') AND FK_BL_NO IN";

	private static final String MARKS_NUMBER_QUERY = "  SELECT RCLTBLS.DEX_BL_MARKS_NO.MARKS_NO,RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.DESCRIPTION,RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.FK_BL_NO FROM RCLTBLS.DEX_BL_MARKS_NO  " + 
			"    INNER JOIN RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC  ON" + 
			"    RCLTBLS.DEX_BL_MARKS_NO.DN_EQUIPMENT_NO=RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.DN_EQUIPMENT_NO" + 
			"    AND RCLTBLS.DEX_BL_MARKS_NO.FK_BL_NO=RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.FK_BL_NO" + 
			"    WHERE RCLTBLS.DEX_BL_MARKS_NO.DN_EQUIPMENT_NO='XXXXXXXXX01'AND RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.FK_BL_NO IN";

	/** QYERY FOR GET ORIGINAL AND UPDETE DATA */
	
	private static final String CONSIGNER_QUERY1 =  "SELECT 'origninal' as type,  FK_BL_NO as FK_BL_NO,FK_CUSTOMER_CODE as CONSIGNER_CODE,CUSTOMER_NAME as CONSIGNER_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,ADDRESS_LINE_3,ADDRESS_LINE_4,CITY,STATE,ZIP,DN_COUNTRY_CODE  FROM RCLTBLS.DEX_BL_CUSTOMERS " + 
			" WHERE CUSTOMER_TYPE ='S'  AND FK_BL_NO in ";
	
	private static final String CONSIGNER_QUERY2 =  " union select  'updated' as type, BL_NO as FK_BL_NO,CONSIGNER_CODE,CONSIGNER_NAME, CONSIGNER_ADDRESS_1 as ADDRESS_LINE_1,CONSIGNER_ADDRESS_2 as ADDRESS_LINE_2,CONSIGNER_ADDRESS_3 as ADDRESS_LINE_3, CONSIGNER_ADDRESS_4 as ADDRESS_LINE_4,CONSIGNER_CITY as CITY, CONSIGNER_STATE as STATE, CONSIGNER_ZIP  as ZIP, CONSIGNER_COUNTRY_CODE as DN_COUNTRY_CODE  from rcl_igm_details " + 
			" WHERE BL_NO IN ";
	
	private static final String CONSIGNEE_QUERY1 =  "SELECT 'origninal' as type,  FK_BL_NO as FK_BL_NO,FK_CUSTOMER_CODE as CONSIGNEE_CODE,CUSTOMER_NAME as CONSIGNEE_NAME,ADDRESS_LINE_1,ADDRESS_LINE_2,ADDRESS_LINE_3,ADDRESS_LINE_4,CITY,STATE,ZIP,DN_COUNTRY_CODE  FROM RCLTBLS.DEX_BL_CUSTOMERS " + 
			" WHERE CUSTOMER_TYPE='C' AND FK_BL_NO in ";

	private static final String CONSIGNEE_QUERY2 =  " union select  'updated' as type, BL_NO as FK_BL_NO,CONSIGNEE_CODE,CONSIGNEE_NAME, CONSIGNEE_ADDRESS_1 as ADDRESS_LINE_1,CONSIGNEE_ADDRESS_2 as ADDRESS_LINE_2,CONSIGNEE_ADDRESS_3 as ADDRESS_LINE_3, CONSIGNEE_ADDRESS_4 as ADDRESS_LINE_4,CONSIGNEE_CITY as CITY, CONSIGNEE_STATE as STATE, CONSIGNEE_ZIP  as ZIP, CONSIGNEE_COUNTRY_CODE as DN_COUNTRY_CODE  from rcl_igm_details " + 
			" WHERE BL_NO IN ";

	private static final String CONTAINER_DETAILS_QUERY1 = "SELECT 'origninal' as type,RCLTBLS.DEX_BL_CONTAINERS.FK_BL_NO,DN_CONTAINER_NO, RCLTBLS.DEX_BL_CONTAINERS.CARRIER_SEAL ," + 
			"            RCLTBLS.DEX_BL_CONTAINERS.EQUIPMENT_STATUS,RCLTBLS.DEX_BL_CONTAINERS.QTY_PACKAGES,         " + 
			"            RCLTBLS.DEX_BL_CONTAINERS.METRIC_WEIGHT,IDP055.EYEQSZ AS CONTAINERSIZE,IDP055.EYEQTP  AS CONTAINERTYPE, null as EQUIPMENT_LOAD_STATUS,null as SOC_FLAG,null as EQUIPMENT_SEAL_TYPE,null as PARAM_VALUE " + 
			"			 FROM RCLTBLS.DEX_BL_CONTAINERS INNER JOIN IDP055 ON IDP055.EYBLNO=RCLTBLS.DEX_BL_CONTAINERS.FK_BL_NO " + 
			"            AND IDP055.EYEQNO=RCLTBLS.DEX_BL_CONTAINERS.DN_CONTAINER_NO  " + 
			"			 WHERE  RCLTBLS.DEX_BL_CONTAINERS.FK_BL_NO IN ";

	private static final String CONTAINER_DETAILS_QUERY2 = "union select  'updated' as type,BL_NO AS FK_BL_NO,CONTAINER_NO AS DN_CONTAINER_NO,CONTAINER_SEAL_NO AS CUSTOMER_SEAL_NO,CONTAINER_STATUS AS EQUIPMENT_STATUS,TOTAL_NO_OF_PACKAGES AS QTY_PACKAGES,CONTAINER_WEIGHT AS METRIC_WEIGHT,CONTAINERSIZE AS CONTAINERSIZE ,CONTAINERTYPE AS CONTAINERTYPE,EQUIPMENT_LOAD_STATUS as EQUIPMENT_LOAD_STATUS,SOC_FLAG AS SOC_FLAG,EQUIPMENT_SEAL_TYPE as EQUIPMENT_SEAL_TYPE,CONTAINER_AGENT_CODE AS PARAM_VALUE " + 
			"	 FROM RCL_IGM_CONTAINERDTLS  WHERE ";
			
	private static final String CONTAINER_DETAILS_QUERY3 = " BL_NO IN ";

	private static final String NOTIFY_PARTY_QUERY1 = "SELECT 'origninal' as type, FK_BL_NO, FK_CUSTOMER_CODE, CUSTOMER_NAME, ADDRESS_LINE_1, ADDRESS_LINE_2, ADDRESS_LINE_3, ADDRESS_LINE_4, CITY, STATE, DN_COUNTRY_CODE, ZIP FROM RCLTBLS.DEX_BL_CUSTOMERS WHERE CUSTOMER_TYPE IN('N','1','2','3') AND FK_BL_NO IN";

	private static final String NOTIFY_PARTY_QUERY2 = "union select  'updated' as type,BL_NO as FK_BL_NO,NOTIFY_CODE as FK_CUSTOMER_CODE,NOTIFY_NAME as CUSTOMER_NAME, NOTIFY_ADDRESS_1 as ADDRESS_LINE_1,NOTIFY_ADDRESS_2 as ADDRESS_LINE_2,NOTIFY_ADDRESS_3 as ADDRESS_LINE_3,NOTIFY_ADDRESS_4 as ADDRESS_LINE_4,NOTIFY_CITY as CITY,NOTIFY_STATE as STATE,NOTIFY_COUNTRY_CODE as DN_COUNTRY_CODE,NOTIFY_ZIP as ZIP FROM rcl_igm_details WHERE BL_NO IN ";

	private static final String MARKS_NUMBER_QUERY1 = "  SELECT 'origninal' as type, RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.FK_BL_NO AS FK_BL_NO,DBMS_LOB.SUBSTR(RCLTBLS.DEX_BL_MARKS_NO.MARKS_NO, 4000, 1) AS MARKS_NO ,DBMS_LOB.SUBSTR(RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.DESCRIPTION, 4000, 1) AS DESCRIPTION FROM RCLTBLS.DEX_BL_MARKS_NO  " + 
			"    INNER JOIN RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC  ON" + 
			"    RCLTBLS.DEX_BL_MARKS_NO.DN_EQUIPMENT_NO=RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.DN_EQUIPMENT_NO" + 
			"    AND RCLTBLS.DEX_BL_MARKS_NO.FK_BL_NO=RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.FK_BL_NO" + 
			"    WHERE RCLTBLS.DEX_BL_MARKS_NO.DN_EQUIPMENT_NO='XXXXXXXXX01'AND RCLTBLS.DEX_BL_COMM_EQUIPMENT_DESC.FK_BL_NO IN";

	private static final String MARKS_NUMBER_QUERY2 =  "union select  'updated' as type,BL_NO as FK_BL_NO,DBMS_LOB.SUBSTR(MARKS_AND_NUMBER, 4000, 1) as MARKS_NO, DBMS_LOB.SUBSTR(DESCRIPTION, 4000, 1) as DESCRIPTION from rcl_igm_details  WHERE BL_NO IN";

	private static final String  MARKS_NUMBER_QUERY3 = "SUBSTR( (SELECT MYMKNO FROM sealiner.idp061" +
			           " WHERE MYBLNO='SHACB21022749'"
			+ "AND MYCNTR  ='XXXXXXXXX01'"
			+ "AND rownum  =1\r\n"
			+ "), INSTR ((SELECT MYMKNO\r\n"
			+ "FROM sealiner.idp061\r\n"
			+ "WHERE MYBLNO='SHACB21022749'\r\n"
			+ "AND MYCNTR  ='XXXXXXXXX01'\r\n"
			+ "AND rownum  =1\r\n"
			+ "), '(S)', -1 )+3,512) ";
	
	/**END OF LIST OF QUERY FOR  GET ORIGINAL AND UPDETE DATA*/
	
	private static final String roadCarrCodeQUerry = " select PARTNER_VALUE,DESCRIPTION from EDI_TRANSLATION_DETAIL where ETH_UID in (" + 
			"  select ETH_UID from EDI_TRANSLATION_HEADER where CODE_SET='ROADCARCOD' " + 
			"  ) and SEALINER_VALUE= ";
	
	private static final String TPBondNoQuery="  select PARTNER_VALUE ,DESCRIPTION  from EDI_TRANSLATION_DETAIL where ETH_UID in (" + 
			"             select ETH_UID from EDI_TRANSLATION_HEADER where CODE_SET='TPBONDNO' ) and SEALINER_VALUE in (";
	
	private static final String CFSCustomcodeQuery=" select PARTNER_VALUE , SEALINER_VALUE from EDI_TRANSLATION_DETAIL where ETH_UID in (" + 
			"             select ETH_UID from EDI_TRANSLATION_HEADER where CODE_SET='IGM_CFS'" + 
			"           ) and SEALINER_VALUE in ";
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rclgroup.dolphin.ezl.web.ell.dao.ImportGeneralManifestDao#getIGMData(java
	 * .util.Map)
	 */
	@Override
	public Map getIGMData(Map amapParam) throws BusinessException, DataAccessException {
		System.out.println("#IGMLogger getIGMData() started..");
		String[][] arrParam = { { KEY_REF_IGM_DATA, BLANK + ORACLE_CURSOR, PARAM_OUT, BLANK },
				{ KEY_IGM_POD, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_POD) },
				{ KEY_IGM_BL, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_BL) },
				{ KEY_IGM_SERVICE, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_SERVICE) },
				{ KEY_IGM_VESSEL, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_VESSEL) },
				{ KEY_IGM_VOYAGE, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_VOYAGE) },
				{ KEY_IGM_POD_TERMINAL, BLANK + ORACLE_VARCHAR, PARAM_IN,(String) amapParam.get(KEY_IGM_POD_TERMINAL) },
				{ KEY_IGM_FROM_DATE, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_FROM_DATE) },
				{ KEY_IGM_TO_DATE, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_TO_DATE) },
				{ KEY_IGM_BL_STATUS, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_BL_STATUS) },
				{ KEY_IGM_POL, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_POL) },
				{ KEY_IGM_DEL, BLANK + ORACLE_VARCHAR, PARAM_IN,(String) amapParam.get(KEY_IGM_DEL)},
				{ KEY_IGM_DEPOT, BLANK + ORACLE_VARCHAR, PARAM_IN,(String) amapParam.get(KEY_IGM_DEPOT)},
				{ KEY_IGM_POL_TERMINAL, BLANK + ORACLE_VARCHAR, PARAM_IN,(String) amapParam.get(KEY_IGM_POL_TERMINAL) },
				{ KEY_IGM_DIRECTION, BLANK + ORACLE_VARCHAR, PARAM_IN, null },
				{ KEY_IGM_ERROR, BLANK + ORACLE_VARCHAR, PARAM_OUT, BLANK } };

		 this.blrNumber  = new HashSet<String>();
		
		 this.PodTerminal = new HashSet<String>();

		
		/* stored procedure object */
		JdbcStoredProcedure objSP = new JdbcStoredProcedure(getDataSource(), SQL_GET_IGM_DATA,
				new ImportGeneralManifestRowMapper(), arrParam);

		/* Return Result from SP execute */
		Map mapResult = objSP.execute();

		/* Stores return error code from SP */
		String strRetError = (String) mapResult.get(KEY_IGM_ERROR);
		/* If return error code is Failure, throw Business Exception */
		if (isErrorCode(strRetError)) {
			System.out.println("Error while getting igm data from DB : " + strRetError);
			throw ExceptionFactory.createApplicationException(strRetError);
		}else {
			if(!blrNumber.isEmpty()) {
			Connection con = null;
			Statement pre = null ;
			ResultSet rs = null;
			try {
				List<ImportGeneralManifestMod> result = (List<ImportGeneralManifestMod>) mapResult.get(ImportGeneralManifestDao.KEY_REF_IGM_DATA);
				//Map<String,Consignee> consigneeMap= new HashMap<String, Consignee>();
				Map<String,List<Consignee>> consigneeMap= new HashMap<String, List<Consignee>>();

				Map<String,List<Consigner>> consignerMap= new HashMap<String, List<Consigner>>();
				
				Map<String, List<ContainerDetails>> containerDetailsMap = new HashMap<String, List<ContainerDetails>>();

				Map<String, List<NotifyParty>> notifyPartyMap = new HashMap<String, List<NotifyParty>>();

				Map<String,List<MarksNumber>> marksNumbersMap= new HashMap<String, List<MarksNumber>>();

				con = objSP.getJdbcTemplate().getDataSource().getConnection();
				
				String query = "select * from ("+CONSIGNEE_QUERY1+ " (";
				String inParam="";
				for(String  bl : blrNumber ) {
					if("".equalsIgnoreCase(inParam)) {
						inParam+="?";
					}else {
					inParam +=  ",?";
					}
				}
				query +=inParam+" )";
				query += CONSIGNEE_QUERY2+ " (";
				query += inParam+ " )";
				query +=") order by type asc";
				PreparedStatement pre1 = con.prepareStatement(query);
				int index=1;
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				rs =  pre1.executeQuery();
				Consignee consignee = null;
				while(rs.next()) {
					consignee= new Consignee();
					consignee.setBlNO(rs.getString("FK_BL_NO"));
					consignee.setCustomerCode(rs.getString("CONSIGNEE_CODE"));
					consignee.setCustomerName(rs.getString("consignee_name"));
					consignee.setAddressLine1(rs.getString("ADDRESS_LINE_1"));
					consignee.setAddressLine2(rs.getString("ADDRESS_LINE_2"));
					consignee.setAddressLine3(rs.getString("ADDRESS_LINE_3"));
					consignee.setAddressLine4(rs.getString("ADDRESS_LINE_4"));
					consignee.setCity(rs.getString("CITY"));
					consignee.setState(rs.getString("STATE"));
					consignee.setCountryCode(rs.getString("DN_COUNTRY_CODE"));
					consignee.setZip(rs.getString("ZIP"));
					

					if(consigneeMap.get(consignee.getBlNO()) == null || "updated".equals(rs.getString("type")) ) {
						List<Consignee> listOfConsine = new ArrayList<Consignee>();


						consigneeMap.put(consignee.getBlNO(), listOfConsine);
					}

					consigneeMap.get(consignee.getBlNO()).add(consignee);

				}
				for(ImportGeneralManifestMod data : result) {
					if((consigneeMap.get(data.getBl()))!=null)
					data.getConsignee().addAll(consigneeMap.get(data.getBl()));				

				}
				
				if(rs != null)
					rs.close();
				if(pre1!=null)
					pre1.close();
				
                String ConsignerQuery = "select * from ("+CONSIGNER_QUERY1+ " (";
				
				ConsignerQuery +=inParam+" )";
				ConsignerQuery += CONSIGNER_QUERY2+ " (";
				ConsignerQuery += inParam+ " )";
				ConsignerQuery +=") order by type asc";
				pre1 = con.prepareStatement(ConsignerQuery);
				index=1;
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				rs =  pre1.executeQuery();
				Consigner consigner = null;
				while(rs.next()) {
					consigner= new Consigner();
					consigner.setBlNO(rs.getString("FK_BL_NO"));
					consigner.setCustomerCode(rs.getString("CONSIGNER_CODE"));
					consigner.setCustomerName(rs.getString("CONSIGNER_NAME"));
					consigner.setAddressLine1(rs.getString("ADDRESS_LINE_1"));
					consigner.setAddressLine2(rs.getString("ADDRESS_LINE_2"));
					consigner.setAddressLine3(rs.getString("ADDRESS_LINE_3"));
					consigner.setAddressLine4(rs.getString("ADDRESS_LINE_4"));
					consigner.setCity(rs.getString("CITY"));
					consigner.setState(rs.getString("STATE"));
					consigner.setCountryCode(rs.getString("DN_COUNTRY_CODE"));
					consigner.setZip(rs.getString("ZIP"));
					

					if(consignerMap.get(consigner.getBlNO()) == null || "updated".equals(rs.getString("type")) ) {
						List<Consigner> listOfConsiner = new ArrayList<Consigner>();


						consignerMap.put(consigner.getBlNO(), listOfConsiner);
					}

					consignerMap.get(consigner.getBlNO()).add(consigner);

				}
				for(ImportGeneralManifestMod data : result) {
					if((consignerMap.get(data.getBl()))!=null)
					data.getConsigner().addAll(consignerMap.get(data.getBl()));				

				}
				
				if(rs != null)
					rs.close();
				if(pre1!=null)
					pre1.close();
				
				String marksNumberQuery ="select * from ("+ MARKS_NUMBER_QUERY1+ " (";
				marksNumberQuery +=inParam+" )";
				marksNumberQuery += MARKS_NUMBER_QUERY2+ " (";
				marksNumberQuery += inParam+" )";
				marksNumberQuery += ") order by type asc";
				//rs = pre.executeQuery(marksNumberQuery);
				
				 pre1 = con.prepareStatement(marksNumberQuery);
				 index=1;
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				rs =  pre1.executeQuery();
				
				MarksNumber marksnumber=null;
				while(rs.next())
				{
					marksnumber=new MarksNumber();
					marksnumber.setBlNO(rs.getString("FK_BL_NO"));
//					marksnumber.setMarksNumbers(rs.getString("MARKS_NO"));
			         String s2 = "(S)";
		             try {
		             if(rs.getString("MARKS_NO")!= null || !rs.getString("MARKS_NO").equals("null")) {
//		            	 int i = rs.getString("MARKS_NO").lastIndexOf(s2);
		     			if(rs.getString("MARKS_NO").lastIndexOf("(S)") != -1)
		     			{
		     				 int i = rs.getString("MARKS_NO").lastIndexOf(s2);
//		     				System.out.println(rs.getString("MARKS_NO").substring(i+3));
		     				marksnumber.setMarksNumbers(rs.getString("MARKS_NO").substring(i+3));
		     			}else {
		     				marksnumber.setMarksNumbers(rs.getString("MARKS_NO"));
		     			}
		     			
		            }else {
		            	marksnumber.setMarksNumbers(rs.getString("MARKS_NO"));
		            }
		             }catch (Exception e) {
		            	 marksnumber.setMarksNumbers(rs.getString("MARKS_NO"));
					}
		             if(null != rs.getString("DESCRIPTION") && !("").equals(rs.getString("DESCRIPTION")) ) {
		            		if(!rs.getString("DESCRIPTION").contains("\r\n")) {
								String Description = rs.getString("DESCRIPTION").replace("\\s+", "");
								
							} 
		             }
				
					marksnumber.setDescription(rs.getString("DESCRIPTION"));
					if(marksNumbersMap.get(marksnumber.getBlNO())==null || "updated".equals(rs.getString("type")) )
					{
						List<MarksNumber> listofMarksNumber=new ArrayList<MarksNumber>();
						marksNumbersMap.put(marksnumber.getBlNO(), listofMarksNumber);
					}
					marksNumbersMap.get(marksnumber.getBlNO()).add(marksnumber);
				}

				for(ImportGeneralManifestMod data : result) {
					if((marksNumbersMap.get(data.getBl()))!=null)
					data.getMarksNumber().addAll(marksNumbersMap.get(data.getBl()));				
				}
				if(rs != null)
					rs.close();
				if(pre1 != null)
					pre1.close();
				String contDetailsQuery = "select * from ("+ CONTAINER_DETAILS_QUERY1+ " (";
				contDetailsQuery += inParam+" )";
				contDetailsQuery +=  CONTAINER_DETAILS_QUERY2;
				contDetailsQuery += " CONTAINER_VESSEL = '"+(String) amapParam.get(KEY_IGM_VESSEL)+"' AND CONTAINER_VOYAGE ='"+(String) amapParam.get(KEY_IGM_VOYAGE)+"' AND CONTAINER_POD='"+(String) amapParam.get(KEY_IGM_POD)+"' AND ";
				contDetailsQuery += CONTAINER_DETAILS_QUERY3 + "(";
				contDetailsQuery +=	inParam+" )";
				contDetailsQuery += ") order by type asc";

				//rs = pre.executeQuery(contDetailsQuery);
				 pre1 = con.prepareStatement(contDetailsQuery);
				 index=1;
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				rs =  pre1.executeQuery();
				
				ContainerDetails contDetails = null; 
				int isUpdatedIndex = 1;
				String oldBL = "";
				while(rs.next())
				{
					contDetails = new ContainerDetails();
					contDetails.setBlNo(rs.getString("FK_BL_NO"));
					contDetails.setContainerNumber(rs.getString("DN_CONTAINER_NO"));
					contDetails.setContainerSealNumber(rs.getString("CARRIER_SEAL"));
					contDetails.setStatus(rs.getString("EQUIPMENT_STATUS"));
					contDetails.setTotalNumberOfPackagesInContainer(rs.getString("QTY_PACKAGES"));
					contDetails.setContainerWeight(rs.getString("METRIC_WEIGHT"));
					contDetails.setContainerSize(rs.getString("CONTAINERSIZE"));
					contDetails.setContainerType(rs.getString("CONTAINERTYPE"));
					contDetails.setContainerAgentCode(rs.getString("PARAM_VALUE"));
					contDetails.setEquipmentLoadStatus(rs.getString("EQUIPMENT_LOAD_STATUS"));
					contDetails.setSoc_flag(rs.getString("SOC_FLAG"));
					contDetails.setEquipment_seal_type(rs.getString("EQUIPMENT_SEAL_TYPE"));

					if(!oldBL.equals(rs.getString("FK_BL_NO"))) {
						isUpdatedIndex=1;
					}
					if("updated".equals(rs.getString("type")) && isUpdatedIndex==1){
						isUpdatedIndex++;
						//containerDetailsMap=new HashMap<String, List<ContainerDetails>>();
						List<ContainerDetails> listOfContDetails = new ArrayList<ContainerDetails>();
						containerDetailsMap.put(contDetails.getBlNo(), listOfContDetails);
					}
					if(containerDetailsMap.get(contDetails.getBlNo()) == null ) {
						 
						List<ContainerDetails> listOfContDetails = new ArrayList<ContainerDetails>();
						containerDetailsMap.put(contDetails.getBlNo(), listOfContDetails);
					}
					containerDetailsMap.get(contDetails.getBlNo()).add(contDetails);
					oldBL = rs.getString("FK_BL_NO");
				}
				for(ImportGeneralManifestMod data : result) {
					if((containerDetailsMap.get(data.getBl()))!=null)
					data.getContainerDetailes().addAll(containerDetailsMap.get(data.getBl()));		
				}

				if(rs != null)
					rs.close();
				if(pre1!= null)
					pre1.close();
				
				String notifyPartyQuery = "select * from ("+NOTIFY_PARTY_QUERY1+ " (";
				notifyPartyQuery += inParam+" )";
				notifyPartyQuery +=NOTIFY_PARTY_QUERY2+ " (";
				notifyPartyQuery +=inParam+" )";
				notifyPartyQuery +=") order by type asc";
				//rs = pre.executeQuery(notifyPartyQuery);
				
				 pre1 = con.prepareStatement(notifyPartyQuery);
				index=1;
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				rs =  pre1.executeQuery();
				
				NotifyParty notifyParty = null;
				while(rs.next())
				{
					notifyParty = new NotifyParty();
					notifyParty.setBlNo(rs.getString("FK_BL_NO"));
					notifyParty.setCostumerCode(rs.getString("FK_CUSTOMER_CODE"));
					notifyParty.setCostumerName(rs.getString("CUSTOMER_NAME"));
					notifyParty.setAddressLine1(rs.getString("ADDRESS_LINE_1"));
					notifyParty.setAddressLine2(rs.getString("ADDRESS_LINE_2"));
					notifyParty.setAddressLine3(rs.getString("ADDRESS_LINE_3"));
					notifyParty.setAddressLine4(rs.getString("ADDRESS_LINE_4"));
					notifyParty.setCity(rs.getString("CITY"));
					notifyParty.setState(rs.getString("STATE"));
					notifyParty.setCountryCode(rs.getString("DN_COUNTRY_CODE"));
					notifyParty.setZip(rs.getString("ZIP"));
					if(notifyPartyMap.get(notifyParty.getBlNo()) == null || "updated".equals(rs.getString("type")) ) {
						List<NotifyParty> listOfNotifyParty = new ArrayList<NotifyParty>();
						notifyPartyMap.put(notifyParty.getBlNo(), listOfNotifyParty);
					}
					notifyPartyMap.get(notifyParty.getBlNo()).add(notifyParty);
				}
				for(ImportGeneralManifestMod data : result) {
					if((notifyPartyMap.get(data.getBl()))!=null)
					data.getNotifyParty().addAll(notifyPartyMap.get(data.getBl()));		
				}
			
				if(pre1 != null )
					pre1.close();
				

			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				try {
					if(rs != null)
						rs.close();
					if(pre != null )
						pre.close();
					if(con != null )
						con.close();
				}catch (Exception e) {
				}

			}
		}
	}
		return mapResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rclgroup.dolphin.ezl.web.ell.dao.ImportGeneralManifestDao#saveIGMData(
	 * java.util.Map)
	 */
	@Override
	public Map saveIGMData(Map amapParam) throws BusinessException, DataAccessException {
		System.out.println("#IGMLogger saveIGMData() started..");

		System.out.println("amapParam :"+amapParam);

		String[][] arrParam = { 
				{	KEY_IGM_SERVICE			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SERVICE	) },
				{	KEY_IGM_VESSEL			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_VESSEL	) },
				{	KEY_IGM_VOYAGE			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_VOYAGE	) },
				{	KEY_IGM_PORT			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT	) },
				{	KEY_IGM_TERMINAL		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TERMINAL	) },
				{	KEY_IGM_NEW_VESSEL		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NEW_VESSEL	) },
				{	KEY_IGM_NEW_VOYAGE		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NEW_VOYAGE	) },
				{	KEY_IGM_FROM_ITEM_NO	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_FROM_ITEM_NO	) },
				{	KEY_IGM_TO_ITEM_NO		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TO_ITEM_NO	) },
				{	KEY_IGM_ROAD_CARR_CODE	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ROAD_CARR_CODE	) },
				{	KEY_IGM_ROAD_TP_BOND_NO	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ROAD_TP_BOND_NO	) },
				{	KEY_IGM_CUST_CODE		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CUST_CODE ) },
				{	KEY_IGM_CALL_SIGN		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CALL_SIGN ) },
				{	KEY_IGM_IMO_CODE		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_IMO_CODE	) },
				{	KEY_IGM_AGENT_CODE		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_AGENT_CODE	) },
				{	KEY_IGM_LINE_CODE		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_LINE_CODE	) },
				{	KEY_IGM_PORT_ORIGIN		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_ORIGIN	) },
				{	KEY_IGM_PORT_ARRIVAL	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_ARRIVAL	) },
				{	KEY_IGM_LAST_PORT_1		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_LAST_PORT_1	) },
				{	KEY_IGM_LAST_PORT_2		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_LAST_PORT_2	) },
				{	KEY_IGM_LAST_PORT_3		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_LAST_PORT_3	) },
				{	KEY_IGM_VESSEL_TYPE		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_VESSEL_TYPE	) },
				{	KEY_IGM_GEN_DESC		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_GEN_DESC	) },
				{	KEY_IGM_MASTER_NAME		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_MASTER_NAME	) },
				{	KEY_IGM_IGM_NUMBER		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_IGM_NUMBER	) },
				{	KEY_IGM_IGM_DATE		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_IGM_DATE	) },
				{	KEY_IGM_VESSEL_NATION	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_VESSEL_NATION	) },
				{	KEY_IGM_ARRIVAL_DATE_ETA	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ARRIVAL_DATE_ETA	) },
				{	KEY_IGM_ARRIVAL_TIME_ETA	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ARRIVAL_TIME_ETA	) },
				{	KEY_IGM_ARRIVAL_DATE_ATA	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ARRIVAL_DATE_ATA	) },
				{	KEY_IGM_ARRIVAL_TIME_ATA	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ARRIVAL_TIME_ATA	) },
				{	KEY_IGM_TOTAL_BLS			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TOTAL_BLS	) },
				{	KEY_IGM_LIGHT_DUE			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_LIGHT_DUE	) },
				{	KEY_IGM_GROSS_WEIGHT		, BLANK + ORACLE_VARCHAR, PARAM_IN,  (String) amapParam.get(	KEY_IGM_GROSS_WEIGHT	) },
				{	KEY_IGM_NET_WEIGHT			, BLANK + ORACLE_VARCHAR, PARAM_IN,  (String) amapParam.get(	KEY_IGM_NET_WEIGHT	) },
				{	KEY_IGM_SM_BT_CARGO			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SM_BT_CARGO	) },
				{	KEY_IGM_SHIP_STR_DECL		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SHIP_STR_DECL	) },
				{	KEY_IGM_CREW_LST_DECL		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CREW_LST_DECL	) },
				{	KEY_IGM_CARGO_DECL			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CARGO_DECL	) },
				{	KEY_IGM_PASSNGR_LIST		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PASSNGR_LIST	) },
				{	KEY_IGM_CREW_EFFECT			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CREW_EFFECT	) },
				{	KEY_IGM_MARITIME_DECL		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_MARITIME_DECL	) },
				{	KEY_IGM_ITEM_NUMBER			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ITEM_NUMBER	) },
				{	KEY_IGM_BL_NO				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_BL_NO	) },
				{	KEY_IGM_CFS_NAME			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CFS_NAME	) },
				{	KEY_IGM_CARGO_NATURE		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CARGO_NATURE	) },
				{	KEY_IGM_CARGO_MOVMNT		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CARGO_MOVMNT	) },
				{	KEY_IGM_ITEM_TYPE			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ITEM_TYPE	) },
				{	KEY_IGM_CARGO_MOVMNT_TYPE	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CARGO_MOVMNT_TYPE	) },
				{	KEY_IGM_TRANSPORT_MODE		, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TRANSPORT_MODE	) },
				{	KEY_IGM_SUBMIT_DATE_TIME	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SUBMIT_DATE_TIME	) },
				{	KEY_IGM_DIRECTION			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_DIRECTION ) },
				{	KEY_IGM_BL_DATE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_BL_DATE	) },
				{	KEY_IGM_MBL_NO				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_MBL_NO	) },
				{	KEY_IGM_HBL_NO				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_HBL_NO	) },
				{	KEY_IGM_PACKAGES			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PACKAGES	) },
				{	KEY_IGM_NHAVA_SHEVA_ETA			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NHAVA_SHEVA_ETA	) },
				{	KEY_IGM_FINAL_PLACE_DELIVERY	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_FINAL_PLACE_DELIVERY	) },
				{	KEY_IGM_POL						, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_POL	) },
				{	KEY_IGM_POL_TERMINAL			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_POL_TERMINAL) },
				
				{	KEY_IGM_DEL						, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_DEL	) },
				{	KEY_IGM_DEPOT			, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_DEPOT ) },
				
				{	KEY_IGM_BL_STATUS				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_BL_STATUS ) },
				{	KEY_IGM_ADD_USER				, BLANK + ORACLE_VARCHAR, PARAM_IN, BLANK },
				{	KEY_IGM_ADD_DATE			, BLANK + ORACLE_VARCHAR, PARAM_IN, BLANK },
				{	KEY_IGM_CHANGE_USER			, BLANK + ORACLE_VARCHAR, PARAM_IN, BLANK },
				{	KEY_IGM_CHANGE_DATE			, BLANK + ORACLE_VARCHAR, PARAM_IN, BLANK },
				{	KEY_IGM_DPD_CODE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_DPD_CODE ) },
				{	KEY_IGM_DPD_MOVEMENT				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_DPD_MOVEMENT ) },
				{	KEY_IGM_BL_VEERSION				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_BL_VEERSION ) },
				{	KEY_IGM_TO_CUSTOM_TERMINAL_CODE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TO_CUSTOM_TERMINAL_CODE ) },
				{	KEY_IGM_CUSTOM_ADD1				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CUSTOM_ADD1 ) },
				{	KEY_IGM_CUSTOM_ADD2				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CUSTOM_ADD2 ) },
				{	KEY_IGM_CUSTOM_ADD3				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CUSTOM_ADD3 ) },
				{	KEY_IGM_CUSTOM_ADD4				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CUSTOM_ADD4 ) },
				{	KEY_IGM_IS_PRESENT_IN_DB	        , BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_IS_PRESENT_IN_DB	) },
				{	KEY_IGM_IS_SELECTED            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_IS_SELECTED	) },
				{	KEY_IGM_COLOR_FLAG            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_COLOR_FLAG	) },
				{	KEY_IGM_PACKAGE_BL_LEVEL            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PACKAGE_BL_LEVEL ) },
				{	KEY_IGM_GROSS_CARGO_WEIGHT_BL_LEVEL            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_GROSS_CARGO_WEIGHT_BL_LEVEL	) },
				{	KEY_IGM_CONSIGNEE_ADD1            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_ADD1	) },
				{	KEY_IGM_CONSIGNEE_ADD2            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_ADD2	) },
				{	KEY_IGM_CONSIGNEE_ADD3            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_ADD3	) },
				{	KEY_IGM_CONSIGNEE_ADD4            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_ADD4	) },
				{	KEY_IGM_CONSIGNEE_COUNTRYCODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_COUNTRYCODE	) },
				{	KEY_IGM_CONSIGNEE_CODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_CODE	) },
				{	KEY_IGM_CONSIGNEE_NAME            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_NAME	) },
				{	KEY_IGM_CONSIGNEE_STATE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_STATE	) },
				{	KEY_IGM_CONSIGNEE_ZIP            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_ZIP	) },
				{	KEY_IGM_CONSIGNEE_CITY            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNEE_CITY	) },
				{	KEY_IGM_MARKSNUMBER_DESCRIPTION            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_MARKSNUMBER_DESCRIPTION	) },
				{	KEY_IGM_MARKSNUMBER_MARKS            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_MARKSNUMBER_MARKS	) },
				{	KEY_IGM_NOTIFYPARTY_ADD1            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_ADD1	) },
				{	KEY_IGM_NOTIFYPARTY_ADD2            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_ADD2	) },
				{	KEY_IGM_NOTIFYPARTY_ADD3            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_ADD3	) },
				{	KEY_IGM_NOTIFYPARTY_ADD4            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_ADD4	) },
				{	KEY_IGM_NOTIFYPARTY_CITY            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_CITY	) },
				{	KEY_IGM_NOTIFYPARTY_COUNTRYCODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_COUNTRYCODE	) },
				{	KEY_IGM_NOTIFYPARTY_CODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_CODE	) },
				{	KEY_IGM_NOTIFYPARTY_NAME            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_NAME	) },
				{	KEY_IGM_NOTIFYPARTY_STATE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_STATE	) },
				{	KEY_IGM_NOTIFYPARTY_ZIP            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NOTIFYPARTY_ZIP	) },
				
				{	KEY_IGM_CONSIGNER_ADD1            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_ADD1	) },
				{	KEY_IGM_CONSIGNER_ADD2            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_ADD2	) },
				{	KEY_IGM_CONSIGNER_ADD3            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_ADD3	) },
				{	KEY_IGM_CONSIGNER_ADD4            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_ADD4	) },
				{	KEY_IGM_CONSIGNER_COUNTRYCODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_COUNTRYCODE	) },
				{	KEY_IGM_CONSIGNER_CODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_CODE	) },
				{	KEY_IGM_CONSIGNER_NAME            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_NAME	) },
				{	KEY_IGM_CONSIGNER_STATE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_STATE	) },
				{	KEY_IGM_CONSIGNER_ZIP            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_ZIP	) },
				{	KEY_IGM_CONSIGNER_CITY            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSIGNER_CITY	) },
				//VESSEL/VOYAGE SECTION START
				
				{	KEY_IGM_NEXT_PORT_1				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NEXT_PORT_1 ) },
				{	KEY_IGM_NEXT_PORT_2				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NEXT_PORT_2 ) },
				{	KEY_IGM_NEXT_PORT_3				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NEXT_PORT_3 ) },
				
				{	KEY_IGM_DEP_MANIF_NO				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_DEP_MANIF_NO ) },
				{	KEY_IGM_DEP_MANIFEST_DATE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_DEP_MANIFEST_DATE ) },
				{	KEY_IGM_SUBMITTER_TYPE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SUBMITTER_TYPE ) },
				{	KEY_IGM_SUBMITTER_CODE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SUBMITTER_CODE ) },
				{	KEY_IGM_AUTHORIZ_REP_CODE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_AUTHORIZ_REP_CODE ) },
				{	KEY_IGM_SHIPPING_LINE_BOND_NO_R				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SHIPPING_LINE_BOND_NO_R ) },
				{	KEY_IGM_MODE_OF_TRANSPORT				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_MODE_OF_TRANSPORT ) },
				{	KEY_IGM_SHIP_TYPE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SHIP_TYPE ) },
				{	KEY_IGM_CONVEYANCE_REFERENCE_NO	        , BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONVEYANCE_REFERENCE_NO	) },
				{	KEY_IGM_CARGO_DESCRIPTION            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CARGO_DESCRIPTION	) },
				{	KEY_IGM_TOL_NO_OF_TRANS_EQU_MANIF            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TOL_NO_OF_TRANS_EQU_MANIF	) },
				{	KEY_IGM_BRIEF_CARGO_DES            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_BRIEF_CARGO_DES	) },
				{	KEY_IGM_EXPECTED_DATE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_EXPECTED_DATE	) },
				{	KEY_IGM_TIME_OF_DEPT            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TIME_OF_DEPT	) },
				{	KEY_IGM_PORT_OF_CALL_COD            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_CALL_COD	) },
				{	KEY_IGM_TOTAL_NO_OF_TRAN_S_CONT_REPO_ON_ARI_DEP            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TOTAL_NO_OF_TRAN_S_CONT_REPO_ON_ARI_DEP	) },
				{	KEY_IGM_MESSAGE_TYPE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_MESSAGE_TYPE	) },
				{	KEY_IGM_VESSEL_TYPE_MOVEMENT            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_VESSEL_TYPE_MOVEMENT	) },
				{	KEY_IGM_AUTHORIZED_SEA_CARRIER_CODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_AUTHORIZED_SEA_CARRIER_CODE	) },
				{	KEY_IGM_PORT_OF_REGISTRY            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_REGISTRY	) },
				{	KEY_IGM_REGISTRY_DATE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_REGISTRY_DATE	) },
				{	KEY_IGM_VOYAGE_DETAILS            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_VOYAGE_DETAILS	) },
				{	KEY_IGM_SHIP_ITINERARY_SEQUENCE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SHIP_ITINERARY_SEQUENCE	) },
				{	KEY_IGM_SHIP_ITINERARY            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SHIP_ITINERARY	) },
				{	KEY_IGM_PORT_OF_CALL_NAME            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_CALL_NAME	) },
				{	KEY_IGM_ARRIVAL_DEPARTURE_DETAILS            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ARRIVAL_DEPARTURE_DETAILS	) },
				{	KEY_IGM_TOTAL_NO_OF_TRANSPORT_EQUIPMENT_REPORTED_ON_ARRIVAL_DEPARTURE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TOTAL_NO_OF_TRANSPORT_EQUIPMENT_REPORTED_ON_ARRIVAL_DEPARTURE	) },
				
				//VESSEL/VOYAGE SECTION END
				//BL SECTION START
				
				{	KEY_IGM_CONSOLIDATED_INDICATOR				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSOLIDATED_INDICATOR ) },
				{	KEY_IGM_PREVIOUS_DECLARATION				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PREVIOUS_DECLARATION ) },
				{	KEY_IGM_CONSOLIDATOR_PAN				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONSOLIDATOR_PAN ) },
				{	KEY_IGM_CIN_TYPE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CIN_TYPE ) },
				{	KEY_IGM_MCIN				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_MCIN ) },
				{	KEY_IGM_CSN_SUBMITTED_TYPE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CSN_SUBMITTED_TYPE ) },
				{	KEY_IGM_CSN_SUBMITTED_BY				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CSN_SUBMITTED_BY ) },
				{	KEY_IGM_CSN_REPORTING_TYPE				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CSN_REPORTING_TYPE ) },
				{	KEY_IGM_CSN_SITE_ID	        , BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CSN_SITE_ID	) },
				{	KEY_IGM_CSN_NUMBER            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CSN_NUMBER	) },
				{	KEY_IGM_CSN_DATE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CSN_DATE	) },
				{	KEY_IGM_PREVIOUS_MCIN            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PREVIOUS_MCIN	) },
				{	KEY_IGM_SPLIT_INDICATOR            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SPLIT_INDICATOR	) },
				{	KEY_IGM_NUMBER_OF_PACKAGES            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NUMBER_OF_PACKAGES	) },
				{	KEY_IGM_TYPE_OF_PACKAGE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TYPE_OF_PACKAGE	) },
				{	KEY_IGM_FIRST_PORT_OF_ENTRY_LAST_PORT_OF_DEPARTURE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_FIRST_PORT_OF_ENTRY_LAST_PORT_OF_DEPARTURE	) },
				{	KEY_IGM_TYPE_OF_CARGO            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TYPE_OF_CARGO	) },
				{	KEY_IGM_SPLIT_INDICATOR_LIST            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_SPLIT_INDICATOR_LIST	) },
				{	KEY_IGM_PORT_OF_ACCEPTANCE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_ACCEPTANCE	) },
				{	KEY_IGM_PORT_OF_RECEIPT            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_RECEIPT	) },
				{	KEY_IGM_UCR_TYPE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_UCR_TYPE	) },
				{	KEY_IGM_UCR_CODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_UCR_CODE	) },
				{	KEY_IGM_PORT_OF_ACCEPTANCE_NAME				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_ACCEPTANCE_NAME ) },
				{	KEY_IGM_PORT_OF_RECEIPT_NAME				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_RECEIPT_NAME ) },
				{	KEY_IGM_PAN_OF_NOTIFIED_PARTY				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PAN_OF_NOTIFIED_PARTY ) },
				{	KEY_IGM_UNIT_OF_WEIGHT				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_UNIT_OF_WEIGHT ) },
				{	KEY_IGM_GROSS_VOLUME				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_GROSS_VOLUME ) },
				{	KEY_IGM_UNIT_OF_VOLUME				, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_UNIT_OF_VOLUME ) },
				{	KEY_IGM_CARGO_ITEM_SEQUENCE_NO	        , BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CARGO_ITEM_SEQUENCE_NO	) },
				{	KEY_IGM_CARGO_ITEM_DESCRIPTION            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CARGO_ITEM_DESCRIPTION	) },
				{	KEY_IGM_CONTAINER_WEIGHT            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_CONTAINER_WEIGHT	) },
				{	KEY_IGM_NUMBER_OF_PACKAGES_HID            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NUMBER_OF_PACKAGES_HID	) },
				{	KEY_IGM_TYPE_OF_PACKAGES_HID            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TYPE_OF_PACKAGES_HID	) },
				{	KEY_IGM_PORT_OF_CALL_SEQUENCE_NUMBER            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_CALL_SEQUENCE_NUMBER	) },
				{	KEY_IGM_PORT_OF_CALL_CODED            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_CALL_CODED	) },
				{	KEY_IGM_NEXT_PORT_OF_CALL_CODED            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_NEXT_PORT_OF_CALL_CODED	) },
				{	KEY_IGM_MC_LOCATION_CUSTOMS            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_MC_LOCATION_CUSTOMS	) },
				{	KEY_IGM_UNO_CODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_UNO_CODE	) },
				{	KEY_IGM_IMDG_CODE            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_IMDG_CODE	) },
				{	KEY_IGM_PORT_OF_DESTINATION            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_PORT_OF_DESTINATION	) },
				{	KEY_IGM_TERMINAL_OP_COD            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_TERMINAL_OP_COD	) },
				{	KEY_IGM_ACTUAL_POD            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_ACTUAL_POD) },
				{	KEY_IGM_IGMPORT            	, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(	KEY_IGM_IGMPORT) },

				{	KEY_IGM_ERROR				, BLANK + ORACLE_VARCHAR, PARAM_OUT, BLANK }

		};

		/* stored procedure object */
		JdbcStoredProcedure objSP = new JdbcStoredProcedure(getDataSource(), SQL_SAVE_IGM_DATA, arrParam);

		/* Return Result from SP execute */
		Map mapResult = objSP.execute();
		/* Stores return error code from SP */
		String strRetError = (String) mapResult.get(KEY_IGM_ERROR);
		/* If return error code is Failure, throw Business Exception */
		if (isErrorCode(strRetError)) {
			throw ExceptionFactory.createApplicationException(strRetError);
		}
		System.out.println("#IGMLogger saveIGMData() completed..");
		return mapResult;
	}


	/**
	 * The Class ImportGeneralManifestRowMapper.
	 */
	private class ImportGeneralManifestRowMapper extends JdbcRowMapper {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.niit.control.dao.JdbcRowMapper#mapRow(java.sql.ResultSet, int)
		 */

		public Object mapRow(ResultSet rs, int row) throws SQLException {
			//System.out.println("#IGMLogger mapRow() started..");
			ImportGeneralManifestMod objMod = new ImportGeneralManifestMod();
			
			objMod.setService(rs.getString("SERVICE"));
			objMod.setBl(rs.getString("BL_NO"));
			blrNumber.add(objMod.getBl());
			objMod.setPod(rs.getString("POD"));
			objMod.setPodTerminal(rs.getString("POD_TERMINAL"));
			PodTerminal.add(objMod.getPodTerminal());
			objMod.setVessel(rs.getString("VESSEL"));
			objMod.setVoyage(rs.getString("VOYAGE"));
			objMod.setCodeCode(rs.getString("CUST_CODE"));
			objMod.setCallSing(rs.getString("CALL_SIGN"));
			objMod.setLineCode(rs.getString("LINE_CODE"));
			objMod.setAgentCode(rs.getString("AGENT_CODE"));
			objMod.setPortOrigin(rs.getString("PORT_ORIGIN"));
			objMod.setPortArrival(rs.getString("PORT_ARRIVAL"));
			objMod.setLastPort1(rs.getString("LAST_PORT_1"));
			objMod.setLastPort2(rs.getString("LAST_PORT_2"));
			objMod.setLastPort3(rs.getString("LAST_PORT_3"));
			objMod.setNextport1(rs.getString("NEXT_PORT_4"));
			objMod.setNextport2(rs.getString("NEXT_PORT_5"));
			objMod.setNextport3(rs.getString("NEXT_PORT_6"));
			objMod.setTerminal(rs.getString("TERMINAL"));
			objMod.setVesselType(rs.getString("VESSEL_TYPE"));
			objMod.setGenDesc(rs.getString("GEN_DESC"));
			objMod.setMasterName(rs.getString("MASTER_NAME"));
			objMod.setVesselNation(rs.getString("VESSEL_NATION"));
			objMod.setIgmNumber(rs.getString("IGM_NUMBER"));
			objMod.setIgmDate(rs.getString("IGM_DATE"));
			objMod.setArrivalDate(rs.getString("ARRIVAL_DATE"));
			objMod.setArrivalTime(rs.getString("ARRIVAL_TIME"));
			objMod.setAtaarrivalDate(rs.getString("ARRIVAL_DATE_ATA"));
			objMod.setAtaarrivalTime(rs.getString("ARRIVAL_TIME_ATA"));
			objMod.setTotalBls(rs.getString("TOTAL_BLS"));
			objMod.setLightDue(rs.getString("LIGHT_DUE"));
			objMod.setSmBtCargo(rs.getString("SM_BT_CARGO"));
			objMod.setShipStrDect(rs.getString("SHIP_STR_DECL"));
			objMod.setCrewEffect(rs.getString("CREW_EFFECT"));
			objMod.setMariTimeDecl(rs.getString("MARITIME_DECL"));
			objMod.setItemNumber(rs.getString("ITEM_NUMBER"));
			objMod.setCargoNature(rs.getString("CARGO_NATURE"));
			objMod.setCargoMovmnt(rs.getString("CARGO_MOVMNT"));
			objMod.setItemType(rs.getString("ITEM_TYPE"));
			objMod.setCargoMovmntType(rs.getString("CARGO_MOVMNT_TYPE"));
			objMod.setTransportMode(rs.getString("TRANSPORT_MODE"));
			objMod.setRoadCarrCode(rs.getString("ROAD_CARR_CODE"));
			objMod.setRoadTpBondNo(rs.getString("ROAD_TP_BOND_NO"));
			objMod.setNewVessel(rs.getString("NEW_VOYAGE"));
			objMod.setNewVoyage(rs.getString("NEW_VESSEL"));
			objMod.setSubmitDateTime(rs.getString("SUBMIT_DATE_TIME"));
			objMod.setNhavaShevaEta(rs.getString("NHAVA_SHEVA_ETA"));
			objMod.setFinalPlaceDelivery(rs.getString("FINAL_PLACE_DELIVERY"));
			objMod.setPackages(rs.getString("PACKAGES"));
			objMod.setCfsName(rs.getString("CFS_NAME"));
			objMod.setMblNo(rs.getString("MBL_NO"));
			objMod.setHblNo(rs.getString("HBL_NO"));
			objMod.setFromItemNo(rs.getString("FROM_ITEM_NO"));
			objMod.setToItemNo(rs.getString("TO_ITEM_NO"));
			objMod.setNetWeight(rs.getString("NET_WEIGHT"));
			objMod.setGrossWeight(rs.getString("GROSS_WEIGHT"));
			objMod.setImoCode(rs.getString("IMO_CODE"));
			objMod.setBlDate(rs.getString("BL_DATE"));
			objMod.setBlStatus(rs.getString("BL_STATUS"));
			objMod.setPol(rs.getString("POL"));
			objMod.setCustomTerminalCode(rs.getString("CUSTOM_TERMINAL_CODE"));
			objMod.setBlVersion(rs.getString("BL_VERSION"));
			objMod.setDpdCode(rs.getString("DPD_CODE"));
			objMod.setDpdMovement(rs.getString("DPD_MOVEMENT"));
			objMod.setPolTerminal(rs.getString("POL_TERMINAL"));
			objMod.setCargoDeclaration(rs.getString("CARGO_DECL"));
			objMod.setCrewListDeclaration(rs.getString("CREW_LST_DECL"));
			objMod.setPassengerList(rs.getString("PASSNGR_LIST"));
			objMod.setCusAdd1(rs.getString("CUSTOMERS_ADDRESS_1"));
			objMod.setCusAdd2(rs.getString("CUSTOMERS_ADDRESS_2"));
			objMod.setCusAdd3(rs.getString("CUSTOMERS_ADDRESS_3"));
			objMod.setCusAdd4(rs.getString("CUSTOMERS_ADDRESS_4"));
			objMod.setIsValidateBL(rs.getString("COLOR_FLAG"));
			objMod.setGrossCargoWeightBLlevel(rs.getString("NET_WEIGHT_METRIC"));
			objMod.setPackageBLLevel(rs.getString("NET_PACKAGE"));
			objMod.setDel(rs.getString("DEL_VLS"));
			objMod.setDepot(rs.getString("DEPOT_VLS"));
			
			//NEW ADDED ROW FOR VESSEL & VOYAGE 
			
			objMod.setDep_manif_no(rs.getString("DEP_MANIF_NO"));
			objMod.setDep_manifest_date(rs.getString("DEP_MANIFEST_DATE"));
			objMod.setSubmitter_type(rs.getString("SUBMITTER_TYPE"));
			objMod.setSubmitter_code(rs.getString("SUBMITTER_CODE"));
			objMod.setAuthoriz_rep_code(rs.getString("AUTHORIZ_REP_CODE"));
			objMod.setShipping_line_bond_no_r(rs.getString("SHIPPING_LINE_BOND_NO_R"));
			objMod.setMode_of_transport(rs.getString("MODE_OF_TRANSPORT"));
			objMod.setShip_type(rs.getString("SHIP_TYPE"));
			objMod.setConveyance_reference_no(rs.getString("CONVEYANCE_REFERENCE_NO"));
			objMod.setCargo_description(rs.getString("CARGO_DESCRIPTION"));
			objMod.setTol_no_of_trans_equ_manif(rs.getString("TOL_NO_OF_TRANS_EQU_MANIF"));
			objMod.setBrief_cargo_des(rs.getString("BRIEF_CARGO_DES"));
			objMod.setExpected_date(rs.getString("EXPECTED_DATE"));
			objMod.setExpected_date(rs.getString("TIME_OF_DEPT"));
			objMod.setTotal_no_of_tran_s_cont_repo_on_ari_dep(rs.getString("TOTAL_NO_OF_TRAN_S_CONT_REPO_ON_ARI_DEP"));
			objMod.setMessage_type(rs.getString("MESSAGE_TYPE"));
			objMod.setVessel_type_movement(rs.getString("VESSEL_TYPE_MOVEMENT"));
			objMod.setAuthorized_sea_carrier_code(rs.getString("AUTHORIZED_SEA_CARRIER_CODE"));
			objMod.setPort_of_registry(rs.getString("PORT_OF_REGISTRY"));
			objMod.setRegistry_date(rs.getString("REGISTRY_DATE"));
			objMod.setVoyage_details_movement(rs.getString("VOYAGE_DETAILS"));
			objMod.setShip_itinerary_sequence(rs.getString("SHIP_ITINERARY_SEQUENCE"));
			objMod.setShip_itinerary(rs.getString("SHIP_ITINERARY"));
			objMod.setPort_of_call_name(rs.getString("PORT_OF_CALL_NAME"));
			objMod.setArrival_departure_details(rs.getString("ARRIVAL_DEPARTURE_DETAILS"));
			objMod.setTotal_no_of_transport_equipment_reported_on_arrival_departure(rs.getString("TOTAL_NO_OF_TRANSPORT_EQUIPMENT_REPORTED_ON_ARRIVAL_DEPARTURE"));
			//vessel&voyage section ended.....
			
			//BL SECTION 
			objMod.setConsolidated_indicator(rs.getString("CONSOLIDATED_INDICATOR"));
			objMod.setPrevious_declaration(rs.getString("PREVIOUS_DECLARATION"));
			objMod.setConsolidator_pan(rs.getString("CONSOLIDATOR_PAN"));
			objMod.setCin_type(rs.getString("CIN_TYPE"));
			objMod.setMcin(rs.getString("MCIN"));
			objMod.setCsn_submitted_type(rs.getString("CSN_SUBMITTED_TYPE"));
			objMod.setCsn_submitted_by(rs.getString("CSN_SUBMITTED_BY"));
			objMod.setCsn_reporting_type(rs.getString("CSN_REPORTING_TYPE"));
			objMod.setCsn_site_id(rs.getString("CSN_SITE_ID"));
			objMod.setCsn_number(rs.getString("CSN_NUMBER"));
			objMod.setCsn_date(rs.getString("CSN_DATE"));
			objMod.setSplit_indicator(rs.getString("PREVIOUS_MCIN"));
			objMod.setSplit_indicator(rs.getString("SPLIT_INDICATOR"));
			objMod.setNumber_of_packages(rs.getString("NUMBER_OF_PACKAGES"));
			objMod.setType_of_package(rs.getString("TYPE_OF_PACKAGE"));
			objMod.setFirst_port_of_entry_last_port_of_departure(rs.getString("FIRST_PORT_OF_ENTRY_LAST_PORT_OF_DEPARTURE"));
			objMod.setType_of_cargo(rs.getString("TYPE_OF_CARGO"));
			objMod.setSplit_indicator_list(rs.getString("SPLIT_INDICATOR_LIST"));
			objMod.setPort_of_acceptance(rs.getString("PORT_OF_ACCEPTANCE"));
			objMod.setPort_of_receipt(rs.getString("PORT_OF_RECEIPT"));
			objMod.setUcr_type(rs.getString("UCR_TYPE"));
			objMod.setUcr_code(rs.getString("UCR_CODE"));
			objMod.setPort_of_acceptance_name(rs.getString("PORT_OF_ACCEPTANCE_NAME"));
			objMod.setPort_of_receipt_name(rs.getString("PORT_OF_RECEIPT_NAME"));
			objMod.setPan_of_notified_party(rs.getString("PAN_OF_NOTIFIED_PARTY"));
			objMod.setUnit_of_weight(rs.getString("UNIT_OF_WEIGHT"));
			objMod.setGross_volume(rs.getString("GROSS_VOLUME"));
			objMod.setUnit_of_volume(rs.getString("UNIT_OF_VOLUME"));
			objMod.setCargo_item_sequence_no(rs.getString("CARGO_ITEM_SEQUENCE_NO"));
			objMod.setCargo_item_description(rs.getString("CARGO_ITEM_DESCRIPTION"));
			objMod.setContainer_weight(rs.getString("CONTAINER_WEIGHT"));
			objMod.setNumber_of_packages_hidden(rs.getString("NUMBER_OF_PACKAGES_HID"));
			objMod.setType_of_packages_hidden(rs.getString("TYPE_OF_PACKAGES_HID"));
			objMod.setPort_of_call_sequence_number(rs.getString("PORT_OF_CALL_SEQUENCE_NUMBER"));
			objMod.setPort_of_call_coded(rs.getString("PORT_OF_CALL_CODED"));
			objMod.setNext_port_of_call_coded(rs.getString("NEXT_PORT_OF_CALL_CODED"));
			objMod.setMc_location_customs(rs.getString("MC_LOCATION_CUSTOMS"));
			objMod.setUno_code(rs.getString("UNO_CODE"));
			objMod.setImdg_code(rs.getString("IMDG_CODE"));
			objMod.setPort_of_destination(rs.getString("port_of_destination"));
			objMod.setTshipmentFlag(rs.getString("TSHIPMNT_FLAG"));
			objMod.setTerminal_op_cod(rs.getString("TERMINAL_OP_COD"));
			
			objMod.setActualPod(rs.getString("ACTUAL_POD"));
//			objMod.setIgmDestinationPort(rs.getString("IGMDEL"));
//			objMod.setIgmport(rs.getString("IGMPORT"));

			if(null != objMod.getActualPod() ) {
			if(objMod.getActualPod().equals(objMod.getPort_of_destination())) {
//				//IGMPORT  implement 
				if(null !=rs.getString("IGMPORT")|| ("").equals(rs.getString("IGMPORT")) ) {
					objMod.setIgmDestinationPort(rs.getString("IGMPORT"));
				}else {
				objMod.setIgmDestinationPort("");
				}
			}else {
				//IGMDEL implement  
				objMod.setIgmDestinationPort(rs.getString("IGMDEL"));
			}
			}

			objMod.setDestinationPortFinal(rs.getString("IGMPORT_DEST"));
			//END BL SECTION 

			return objMod;
		}
	}
	
	public String destinationPortCall() {
		
		return null;
		
	}

	@Override
	public Map getRefreshIGMData(Map amapParam) throws BusinessException, DataAccessException {
		System.out.println("#IGMLogger getIGMData() started..");
		System.out.println("dao getRefresh()"+amapParam.toString());
		String[][] arrParam = { { KEY_REF_IGM_DATA, BLANK + ORACLE_CURSOR, PARAM_OUT, BLANK },
				{ KEY_IGM_POD, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_POD) },
				{ KEY_IGM_BL, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_BL) },
				{ KEY_IGM_SERVICE, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_SERVICE) },
				{ KEY_IGM_VESSEL, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_VESSEL) },
				{ KEY_IGM_VOYAGE, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_VOYAGE) },
				{ KEY_IGM_POD_TERMINAL, BLANK + ORACLE_VARCHAR, PARAM_IN,(String) amapParam.get(KEY_IGM_POD_TERMINAL) },
				{ KEY_IGM_FROM_DATE, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_FROM_DATE) },
				{ KEY_IGM_TO_DATE, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_TO_DATE) },
				{ KEY_IGM_BL_STATUS, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_BL_STATUS) },
				{ KEY_IGM_POL, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_POL) },
				{ KEY_IGM_DEL, BLANK + ORACLE_VARCHAR, PARAM_IN,(String) amapParam.get(KEY_IGM_DEL)},
				{ KEY_IGM_DEPOT, BLANK + ORACLE_VARCHAR, PARAM_IN,(String) amapParam.get(KEY_IGM_DEPOT)},
				{ KEY_IGM_POL_TERMINAL, BLANK + ORACLE_VARCHAR, PARAM_IN,(String) amapParam.get(KEY_IGM_POL_TERMINAL) },
				{ KEY_IGM_DIRECTION, BLANK + ORACLE_VARCHAR, PARAM_IN, null },
				{ KEY_IGM_ERROR, BLANK + ORACLE_VARCHAR, PARAM_OUT, BLANK } };

		
		 this.blrNumber  = new HashSet<String>();
			
		 this.PodTerminal = new HashSet<String>();
		/* stored procedure object */
		JdbcStoredProcedure objSP = new JdbcStoredProcedure(getDataSource(), SQL_GET_REFRESH_IGM_DATA,
				new ImportGeneralManifestRowMapper(), arrParam);

		/* Return Result from SP execute */
		Map mapResult = objSP.execute();

		/* Stores return error code from SP */
		String strRetError = (String) mapResult.get(KEY_IGM_ERROR);
		/* If return error code is Failure, throw Business Exception */
		if (isErrorCode(strRetError)) {
			System.out.println("Error while getting igm data from DB : " + strRetError);
			throw ExceptionFactory.createApplicationException(strRetError);
		}else {
			Connection con = null;
			Statement pre = null ;
			ResultSet rs = null;
			try {
				List<ImportGeneralManifestMod> result = (List<ImportGeneralManifestMod>) mapResult.get(ImportGeneralManifestDao.KEY_REF_IGM_DATA);
				//Map<String,Consignee> consigneeMap= new HashMap<String, Consignee>();
				Map<String,List<Consignee>> consigneeRefreshMap= new HashMap<String, List<Consignee>>();

				Map<String,List<Consigner>> consignerRefreshMap= new HashMap<String, List<Consigner>>();
				
				Map<String, List<ContainerDetails>> containerDetailsRefreshMap = new HashMap<String, List<ContainerDetails>>();

				Map<String, List<NotifyParty>> notifyPartyRefreshMap = new HashMap<String, List<NotifyParty>>();

				Map<String,List<MarksNumber>> marksNumbersRefreshMap= new HashMap<String, List<MarksNumber>>();

				con = objSP.getJdbcTemplate().getDataSource().getConnection();
				
				String query = CONSIGNEE_QUERY+ " (";
				String inParam="";
				for(String  bl : blrNumber ) {
					if("".equalsIgnoreCase(inParam)) {
						inParam+="?";
					}else {
					inParam +=  ",?";
					}
				}
				query +=inParam+" )";
				PreparedStatement pre1 = con.prepareStatement(query);
				int index=1;
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				rs =  pre1.executeQuery();
				Consignee consignee = null;
				while(rs.next()) {
					consignee= new Consignee();
					consignee.setBlNO(rs.getString("FK_BL_NO"));
					consignee.setCustomerCode(rs.getString("FK_CUSTOMER_CODE"));
					consignee.setCustomerName(rs.getString("CUSTOMER_NAME"));
					consignee.setAddressLine1(rs.getString("ADDRESS_LINE_1"));
					consignee.setAddressLine2(rs.getString("ADDRESS_LINE_2"));
					consignee.setAddressLine3(rs.getString("ADDRESS_LINE_3"));
					consignee.setAddressLine4(rs.getString("ADDRESS_LINE_4"));
					consignee.setCity(rs.getString("CITY"));
					consignee.setState(rs.getString("STATE"));
					consignee.setCountryCode(rs.getString("DN_COUNTRY_CODE"));
					consignee.setZip(rs.getString("ZIP"));

					if(consigneeRefreshMap.get(consignee.getBlNO()) == null  ) {
						List<Consignee> listOfConsine = new ArrayList<Consignee>();


						consigneeRefreshMap.put(consignee.getBlNO(), listOfConsine);
					}

					consigneeRefreshMap.get(consignee.getBlNO()).add(consignee);

				}
				for(ImportGeneralManifestMod data : result) {
					data.getConsignee().addAll(consigneeRefreshMap.get(data.getBl()));				

				}
				if(rs != null)
					rs.close();
				if(pre1!=null)
					pre1.close();
				
				
				String consignerQuery = CONSIGNER_QUERY+ " (";
				consignerQuery +=inParam+" )";
			    pre1 = con.prepareStatement(consignerQuery);
			    index=1;
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				rs = pre1.executeQuery();
				Consigner consigner=null;
				while(rs.next())
				{
					consigner= new Consigner();
					consigner.setBlNO(rs.getString("FK_BL_NO"));
					consigner.setCustomerCode(rs.getString("FK_CUSTOMER_CODE"));
					consigner.setCustomerName(rs.getString("CUSTOMER_NAME"));
					consigner.setAddressLine1(rs.getString("ADDRESS_LINE_1"));
					consigner.setAddressLine2(rs.getString("ADDRESS_LINE_2"));
					consigner.setAddressLine3(rs.getString("ADDRESS_LINE_3"));
					consigner.setAddressLine4(rs.getString("ADDRESS_LINE_4"));
					consigner.setCity(rs.getString("CITY"));
					consigner.setState(rs.getString("STATE"));
					consigner.setCountryCode(rs.getString("DN_COUNTRY_CODE"));
					consigner.setZip(rs.getString("ZIP"));
					if(consignerRefreshMap.get(consigner.getBlNO())==null)
					{
						List<Consigner> listofconsigner=new ArrayList<Consigner>();
						consignerRefreshMap.put(consigner.getBlNO(), listofconsigner);
					}
					consignerRefreshMap.get(consigner.getBlNO()).add(consigner);
				}

				for(ImportGeneralManifestMod data : result) {
					data.getConsigner().addAll(consignerRefreshMap.get(data.getBl()));				
				}
				if(rs != null)
					rs.close();
				if(pre1!=null)
					pre1.close();
				
				
				
				String marksNumberQuery = MARKS_NUMBER_QUERY+ " (";
				marksNumberQuery +=inParam+" )";
			    pre1 = con.prepareStatement(marksNumberQuery);
			    index=1;
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				rs = pre1.executeQuery();
				MarksNumber marksnumber=null;
				while(rs.next())
				{
					marksnumber=new MarksNumber();
					marksnumber.setBlNO(rs.getString("FK_BL_NO"));
					marksnumber.setMarksNumbers(rs.getString("MARKS_NO"));
					marksnumber.setDescription(rs.getString("DESCRIPTION"));
					if(marksNumbersRefreshMap.get(marksnumber.getBlNO())==null)
					{
						List<MarksNumber> listofMarksNumber=new ArrayList<MarksNumber>();
						marksNumbersRefreshMap.put(marksnumber.getBlNO(), listofMarksNumber);
					}
					marksNumbersRefreshMap.get(marksnumber.getBlNO()).add(marksnumber);
				}

				for(ImportGeneralManifestMod data : result) {
					data.getMarksNumber().addAll(marksNumbersRefreshMap.get(data.getBl()));				
				}
				if(rs != null)
					rs.close();
				if(pre1!=null)
					pre1.close();
				
				String contDetailsQuery = CONTAINER_DETAILS_QUERY+ " (";
				contDetailsQuery +=  inParam+" )";
				pre1 = con.prepareStatement(contDetailsQuery);
				    index=1;
					for(String  bl : blrNumber ) {
						pre1.setString(index++,bl);
					}
					
				rs = pre1.executeQuery();
				ContainerDetails contDetails = null; 
				while(rs.next())
				{
					contDetails = new ContainerDetails();
					contDetails.setBlNo(rs.getString("FK_BL_NO"));
					contDetails.setContainerNumber(rs.getString("DN_CONTAINER_NO"));
					contDetails.setContainerSealNumber(rs.getString("CARRIER_SEAL"));
					contDetails.setStatus(rs.getString("EQUIPMENT_STATUS"));
					contDetails.setTotalNumberOfPackagesInContainer(rs.getString("QTY_PACKAGES"));
					contDetails.setContainerWeight(rs.getString("METRIC_WEIGHT"));
					contDetails.setContainerSize(rs.getString("CONTAINERSIZE"));
					contDetails.setContainerType(rs.getString("CONTAINERTYPE"));
					if(containerDetailsRefreshMap.get(contDetails.getBlNo()) == null) {
						List<ContainerDetails> listOfContDetails = new ArrayList<ContainerDetails>();
						containerDetailsRefreshMap.put(contDetails.getBlNo(), listOfContDetails);
					}
					containerDetailsRefreshMap.get(contDetails.getBlNo()).add(contDetails);
				}
				for(ImportGeneralManifestMod data : result) {
					data.getContainerDetailes().addAll(containerDetailsRefreshMap.get(data.getBl()));		
				}
				if(rs != null)
					rs.close();
				if(pre1!=null)
					pre1.close();
				
				String notifyPartyQuery = NOTIFY_PARTY_QUERY+ " (";
				notifyPartyQuery +=  inParam+" )";
				pre1 = con.prepareStatement(notifyPartyQuery);
				index=1;
				for(String  bl : blrNumber ) {
					pre1.setString(index++,bl);
				}
				rs = pre1.executeQuery();
				NotifyParty notifyParty = null;
				while(rs.next())
				{
					notifyParty = new NotifyParty();
					notifyParty.setBlNo(rs.getString("FK_BL_NO"));
					notifyParty.setCostumerCode(rs.getString("FK_CUSTOMER_CODE"));
					notifyParty.setCostumerName(rs.getString("CUSTOMER_NAME"));
					notifyParty.setAddressLine1(rs.getString("ADDRESS_LINE_1"));
					notifyParty.setAddressLine2(rs.getString("ADDRESS_LINE_2"));
					notifyParty.setAddressLine3(rs.getString("ADDRESS_LINE_3"));
					notifyParty.setAddressLine4(rs.getString("ADDRESS_LINE_4"));
					notifyParty.setCity(rs.getString("CITY"));
					notifyParty.setState(rs.getString("STATE"));
					notifyParty.setCountryCode(rs.getString("DN_COUNTRY_CODE"));
					notifyParty.setZip(rs.getString("ZIP"));
					if(notifyPartyRefreshMap.get(notifyParty.getBlNo()) == null) {
						List<NotifyParty> listOfNotifyParty = new ArrayList<NotifyParty>();
						notifyPartyRefreshMap.put(notifyParty.getBlNo(), listOfNotifyParty);
					}
					notifyPartyRefreshMap.get(notifyParty.getBlNo()).add(notifyParty);
				}
				for(ImportGeneralManifestMod data : result) {
					data.getNotifyParty().addAll(notifyPartyRefreshMap.get(data.getBl()));		
				}
				if(pre1!=null)
					pre1.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				try {
					if(rs != null)
						rs.close();
					if(pre != null )
						pre.close();
					if(con != null )
						con.close();
				}catch (Exception e) {
				}

			}
		}
		return mapResult;
	}

	@Override
	public Map getSerialNumber(String slNo) throws BusinessException, DataAccessException {
		System.out.println("Method started....... getSerialNumber");
		String[][] arrParam = { { KEY_REF_IGM_DATA, BLANK + ORACLE_VARCHAR, PARAM_OUT, BLANK },
				{ KEY_REF_IGM_DATA_JOB, BLANK + ORACLE_VARCHAR, PARAM_OUT, BLANK },
				{ KEY_IGM_SRL_NO, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) slNo },
				{ KEY_IGM_ERROR, BLANK + ORACLE_VARCHAR, PARAM_OUT, BLANK } };
		/* stored procedure object */
		JdbcStoredProcedure objSP = new JdbcStoredProcedure(getDataSource(), SQL_SERIAL_NUMBER_DATA,
				new ImportGeneralManifestRowMapper(), arrParam);

		/* Return Result from SP execute */
		Map mapResult = objSP.execute();
		String strRetError = (String) mapResult.get(KEY_IGM_ERROR);
		/* If return error code is Failure, throw Business Exception */
		if (isErrorCode(strRetError)) {
			System.out.println("Error while getting igm data from DB : " + strRetError);
			throw ExceptionFactory.createApplicationException(strRetError);
		}
		 Map<String, String> data = new HashMap<String, String>();
		 data.put("selno",mapResult.get(KEY_REF_IGM_DATA).toString() );
		 data.put("jobno",mapResult.get(KEY_REF_IGM_DATA_JOB).toString());
		return data;
	}

	@Override
	public String saveContainerData(String contDtl,Map amapParam) throws BusinessException, DataAccessException {
		System.out.println("saveContainerData() started");
		String[][] arrParam = {
				{ KEY_IGM_POD, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_POD) },
				{ KEY_IGM_VESSEL, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_VESSEL) },
				{ KEY_IGM_VOYAGE, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) amapParam.get(KEY_IGM_VOYAGE) },
				{ KEY_IGM_CONTAINER_DTLS, BLANK + ORACLE_VARCHAR, PARAM_IN, (String) contDtl },
		};

		JdbcStoredProcedure objSP = new JdbcStoredProcedure(getDataSource(), SQL_SAVE_CONTAINOR,
				new ImportGeneralManifestRowMapper(),arrParam);

		objSP.execute();
		return null;
	}

	@Override
	public Map getroadCarrCodeData(Map amapParam) throws BusinessException, DataAccessException {
		Map dropDownMap=new HashMap();
		String podVal= (String) amapParam.get(KEY_IGM_POD) ;
		
		Connection con = null;
		Statement pre = null ;
		ResultSet rs = null;
		
		try {

			//Map<String,List<DropDownMod>> DropDownRoadCarrCodeMap= new HashMap<String, List<DropDownMod>>();
			List<DropDownMod> DropDownRoadCarrCodeList=new ArrayList<>();
			List tepmListTPBondNo=new ArrayList();
			
			con = DataSourceUtils.getConnection(getDataSource());
			pre = con.createStatement();
			
			String queryRoadCarrCode = roadCarrCodeQUerry ; 
			queryRoadCarrCode +="'" +podVal+"'";
			
			rs = pre.executeQuery(queryRoadCarrCode);
			DropDownMod dropdown = null;
			while(rs.next()) {
				dropdown= new DropDownMod();
				dropdown.setPodValue(podVal);
				dropdown.setPartnerValuedre(rs.getString("PARTNER_VALUE"));
				dropdown.setDescriptiondrw(rs.getString("DESCRIPTION"));
				tepmListTPBondNo.add(rs.getString("PARTNER_VALUE"));
			DropDownRoadCarrCodeList.add(dropdown);	
			}
			dropDownMap.put("roadCarrCoadDropdown", DropDownRoadCarrCodeList);
			if(rs != null)
				rs.close();
			
            Map<String,List<DropDownMod>> tpBondNoMap= new HashMap<String, List<DropDownMod>>();
            
			con = DataSourceUtils.getConnection(getDataSource());
			pre = con.createStatement();
			
			String queryTPBondNo = TPBondNoQuery ; 
			queryTPBondNo +="'"+StringUtils.join((tepmListTPBondNo.iterator()),"','")+"' )";
			
			rs = pre.executeQuery(queryTPBondNo);
			DropDownMod dropdowntp = null;
			int i=0;
			while(rs.next()) {
				dropdowntp= new DropDownMod();
				dropdowntp.setPodValue((String)tepmListTPBondNo.get(i));
				dropdowntp.setPartnerValuedre(rs.getString("PARTNER_VALUE"));
				dropdowntp.setDescriptiondrw(rs.getString("DESCRIPTION"));
				i++;
				if(tpBondNoMap.get(dropdowntp.getPodValue()) == null)
				{
				List<DropDownMod>  DropDownTPBondNoList=new  ArrayList<DropDownMod>();
				tpBondNoMap.put(dropdowntp.getPodValue(), DropDownTPBondNoList);
				}
				tpBondNoMap.get(dropdowntp.getPodValue()).add(dropdowntp);
			}
			dropDownMap.put("TPBondNoDropdown", tpBondNoMap);
			
			
			
			if(rs != null)
				rs.close();
			
				Map<String,List<CFSCustomCode>> cfsCustomCodeMap= new HashMap<String, List<CFSCustomCode>>();
				
				con = DataSourceUtils.getConnection(getDataSource());
				pre = con.createStatement();
				
				String querycfscustomcode = CFSCustomcodeQuery+ " (" ; 
				querycfscustomcode +="'"+StringUtils.join(PodTerminal.iterator(),"','")+"')";
				
				rs = pre.executeQuery(querycfscustomcode);
				CFSCustomCode dropdownCfs = null;
				while(rs.next()) {
					dropdownCfs= new CFSCustomCode();
					dropdownCfs.setCfsCustomCode(rs.getString("PARTNER_VALUE"));
					dropdownCfs.setPodTerminal(rs.getString("SEALINER_VALUE"));
				
					if(cfsCustomCodeMap.get(dropdownCfs.getPodTerminal()) == null ) {
						List<CFSCustomCode> listOfCfsCode = new ArrayList<CFSCustomCode>();

						cfsCustomCodeMap.put(dropdownCfs.getPodTerminal(), listOfCfsCode);
					}

					cfsCustomCodeMap.get(dropdownCfs.getPodTerminal()).add(dropdownCfs);
				}
				dropDownMap.put("CFSCustomDropdown", cfsCustomCodeMap);
			
			
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				if(rs != null)
					rs.close();
				if(pre != null )
					pre.close();
				if(con != null )
					con.close();
			}catch (Exception e) {
			}

		}
		
		return dropDownMap;
	}
}
