/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.uvms.plugins.mdr;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.*;

@RunWith(JUnit4.class)
public class TestOracle {

    @Test
    @Ignore
    public void testOracleConn(){
        String message = getMockedMessage();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1522:xe", "flux_msg", "password");
            CallableStatement cstmt = connection.prepareCall("{call bp_mdm_clob_v1(?, ?, ?, ?, ?)}");
            cstmt.setString(1, "GRC");
            cstmt.setString(2, "GRC20180228123");
            cstmt.setString(3, message);
            cstmt.registerOutParameter(4, Types.VARCHAR);
            cstmt.registerOutParameter(5, Types.CLOB);
            cstmt.setQueryTimeout(1800);
            cstmt.executeUpdate();
            String resp2=cstmt.getString(5);
            System.out.println("Ok done : \n\n" + resp2);
        } catch (SQLException ignored) {
            System.out.println(ignored);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                    System.out.println(ignored);
                }
            }
        }
    }

    private String getMockedMessage() {
        String message = "<rsm:FLUXMDRQueryMessage xmlns:ram='urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:20' ";
        message = message + " xmlns:rsm='urn:un:unece:uncefact:data:standard:FLUXMDRQueryMessage:5' ";
        message = message +        " xmlns:udt='urn:un:unece:uncefact:data:standard:UnqualifiedDataType:20' ";
        message = message +        " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'  ";
        message = message + " xsi:schemaLocation='urn:un:unece:uncefact:data:standard:FLUXMDRQueryMessage:5 FLUXMDRQueryMessage_5p0.xsd'> ";
        message = message + "<rsm:MDRQuery> ";
        message = message + " <ram:ID>cd3a451e-b6ba-4b12-b854-7144e2e8565a</ram:ID> ";
        message = message + " <ram:SubmittedDateTime> ";
        message = message + " <udt:DateTime>2016-09-06T12:00:00</udt:DateTime> ";
        message = message + " </ram:SubmittedDateTime> ";
        message = message + " <ram:TypeCode>OBJ_DATA_VERSION</ram:TypeCode> ";
        message = message + " <ram:ContractualLanguageCode>EN</ram:ContractualLanguageCode> ";
        message = message + " <ram:SubmitterFLUXParty> ";
        message = message + " <ram:ID>GRC</ram:ID> ";
        message = message + " </ram:SubmitterFLUXParty> ";
        message = message + " <ram:SubjectMDRQueryIdentity> ";
        message = message + " <ram:ID>FISH_SIZE_CAT</ram:ID>  ";
        message = message + " <ram:VersionID>1.0</ram:VersionID> ";
        message = message + " <!--<ram:ValidityDelimitedPeriod> ";
        message = message + " <ram:StartDateTime> ";
        message = message + " <udt:DateTime>2017-01-01T12:00:00</udt:DateTime> ";
        message = message + " </ram:StartDateTime> ";
        message = message + " <ram:EndDateTime> ";
        message = message + " <udt:DateTime>2016-01-01T12:00:00</udt:DateTime> ";
        message = message + " </ram:EndDateTime> ";
        message = message + " </ram:ValidityDelimitedPeriod>--> ";
        message = message + " </ram:SubjectMDRQueryIdentity> ";
        message = message + " </rsm:MDRQuery> ";
        message = message + " </rsm:FLUXMDRQueryMessage> ";
        return message;
    }
}
