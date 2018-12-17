/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.uvms.plugins.mdr.oracle.dao;

import eu.europa.ec.fisheries.uvms.plugins.mdr.saxparser.MdrSaxaprserAcronymExtractor;
import eu.europa.ec.fisheries.uvms.plugins.mdr.saxparser.MdrType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

@Slf4j
public class MdmOracleDao {

    private EntityManager em;

    public MdmOracleDao() {
        super();
    }

    public MdmOracleDao(EntityManager em) {
        this.em = em;
    }

    public String findCodeListByMdrQuery(String messageToPost, String frParam, String onParam, String storedProcName) {
        final StoredProcedureQuery storedProcedureQuery = em.createStoredProcedureQuery(storedProcName);
        storedProcedureQuery.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter(4, String.class, ParameterMode.OUT);
        storedProcedureQuery.registerStoredProcedureParameter(5, Clob.class, ParameterMode.OUT);
        storedProcedureQuery.setParameter(1, frParam);
        storedProcedureQuery.setParameter(2, onParam);
        storedProcedureQuery.setParameter(3, messageToPost);
        String response;
        try {
            storedProcedureQuery.execute();
            response = getStringFromClob(storedProcedureQuery);
        } catch (Exception ex) {
            log.error("[ERROR] While trying to call Oracle db 'bp_mdm_clob_v1' stored procedure : ", ex);
            response = getErrorResponse(messageToPost);
        }
        return response;
    }

    private String getStringFromClob(StoredProcedureQuery storedProcedureQuery) {
        final Clob respClob = (Clob) storedProcedureQuery.getOutputParameterValue(5);
        final StringBuilder sb = new StringBuilder();
        try {
            final Reader reader = respClob.getCharacterStream();
            final BufferedReader br = new BufferedReader(reader);
            int b;
            while (-1 != (b = br.read())) {
                sb.append((char) b);
            }
            br.close();
        } catch (SQLException | IOException e) {
            log.error("[ERROR] SQL. Could not convert CLOB to string", e);
            return e.toString();
        }
        return sb.toString();
    }

    private EntityManager getEntityManager() {
        return em;
    }

    private String getErrorResponse(String mdrQueryStr) {
        MdrSaxaprserAcronymExtractor acronymExtractor = new MdrSaxaprserAcronymExtractor(MdrType.MDR_QUERY);
        String uuid = StringUtils.EMPTY;
        try {
            acronymExtractor.parseDocument(mdrQueryStr);
        } catch (SAXException e) {
            uuid = acronymExtractor.getUuidValue();
            log.info("[INFO] Foud UUID value", uuid);
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rsm:FLUXMDRReturnMessage xmlns:ram=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:20\" xmlns:rsm=\"urn:un:unece:uncefact:data:standard:FLUXMDRReturnMessage:5\" xmlns:udt=\"urn:un:unece:uncefact:data:standard:UnqualifiedDataType:20\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:un:unece:uncefact:data:standard:FLUXMDRReturnMessage:5 FLUXMDRReturnMessage_5p0.xsd urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:20 ReusableAggregateBusinessInformationEntity_20p0.xsd urn:un:unece:uncefact:data:standard:UnqualifiedDataType:20 UnqualifiedDataType_20p0.xsd\">\n" +
                "<rsm:FLUXResponseDocument>\n" +
                "    <ram:ID>66E4CEC9-ED80-EAF2-E050-12AC040000C0</ram:ID>\n" +
                "    <ram:ReferencedID>"+uuid+"</ram:ReferencedID>\n" +
                "    <ram:CreationDateTime>\n" +
                "        <udt:DateTime>2018-03-08T10:04:56Z</udt:DateTime>\n" +
                "    </ram:CreationDateTime>\n" +
                "    <ram:ResponseCode>NOK</ram:ResponseCode>\n" +
                "    <ram:TypeCode>OBJ_DATA_VERSION</ram:TypeCode>\n" +
                "    <ram:RelatedValidationResultDocument>\n" +
                "        <ram:ValidatorID>XEU</ram:ValidatorID>\n" +
                "        <ram:CreationDateTime>\n" +
                "            <udt:DateTime>2018-03-08T10:04:56Z</udt:DateTime>\n" +
                "        </ram:CreationDateTime>\n" +
                "    </ram:RelatedValidationResultDocument>\n" +
                "    <ram:RespondentFLUXParty>\n" +
                "        <ram:ID>XEU</ram:ID>\n" +
                "    </ram:RespondentFLUXParty>\n" +
                "</rsm:FLUXResponseDocument>\n" +
                "<rsm:MDRDataSet>\n" +
                "    <ram:ID>GEAR_TYPE</ram:ID>\n" +
                "    <ram:Description>List of fish size categories</ram:Description>\n" +
                "    <ram:Origin>MARE</ram:Origin>\n" +
                "    <ram:Name>Fish Size Category</ram:Name>\n" +
                "    <ram:SpecifiedDataSetVersion>\n" +
                "        <ram:ID>1.0</ram:ID>\n" +
                "        <ram:Name>Fish Size Category</ram:Name>\n" +
                "        <ram:ValidityStartDateTime>\n" +
                "            <udt:DateTime>2011-01-01T00:00:00Z</udt:DateTime>\n" +
                "        </ram:ValidityStartDateTime>\n" +
                "        <ram:ValidityEndDateTime>\n" +
                "            <udt:DateTime>2013-09-15T00:00:00Z</udt:DateTime>\n" +
                "        </ram:ValidityEndDateTime>\n" +
                "    </ram:SpecifiedDataSetVersion>\n" +
                "</rsm:MDRDataSet>\n" +
                "</rsm:FLUXMDRReturnMessage>\n";
    }
}
