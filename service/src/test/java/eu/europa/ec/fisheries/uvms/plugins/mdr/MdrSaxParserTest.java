/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.uvms.plugins.mdr;

import eu.europa.ec.fisheries.uvms.plugins.mdr.saxparser.MdrSaxaprserAcronymExtractor;
import eu.europa.ec.fisheries.uvms.plugins.mdr.saxparser.MdrType;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

import java.io.PrintStream;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class MdrSaxParserTest {

    private MdrSaxaprserAcronymExtractor mdrSaxExtractor;

    private String mdrSample;

    private PrintStream logger = System.out;

    @Before
    @SneakyThrows
    public void init(){
        mdrSaxExtractor = new MdrSaxaprserAcronymExtractor(MdrType.MDR_QUERY);
        mdrSample = getMockedMessage();
    }

    @Test
    public void testMdrQueryUUIDExtraction(){
        String messageGuid = null;
        try {
            mdrSaxExtractor.parseDocument(mdrSample);
        } catch (SAXException e) {
            // below message would be thrown once value is found.
            if (e instanceof SAXException){
                messageGuid = mdrSaxExtractor.getUuidValue();
            }
        }
        logger.println("FaQuery GUID : " + messageGuid);
        assertNotNull(messageGuid);
    }


    private String getMockedMessage() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ns3:FLUXMDRQueryMessage xmlns=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:20\" xmlns:ns2=\"urn:un:unece:uncefact:data:standard:UnqualifiedDataType:20\" xmlns:ns3=\"urn:un:unece:uncefact:data:standard:FLUXMDRQueryMessage:5\">\n" +
                "    <ns3:MDRQuery>\n" +
                "        <ID schemeID=\"UUID\">bd5c67a2-9dd8-4399-a2d0-b8a5d56a181f</ID>\n" +
                "        <SubmittedDateTime>\n" +
                "            <ns2:DateTime>2018-03-08T15:17:37.022Z</ns2:DateTime>\n" +
                "        </SubmittedDateTime>\n" +
                "        <TypeCode listID=\"FLUX_MDR_QUERY_TYPE\">OBJ_DATA_ALL</TypeCode>\n" +
                "        <ContractualLanguageCode>EN</ContractualLanguageCode>\n" +
                "        <SubmitterFLUXParty>\n" +
                "            <ID>BEL</ID>\n" +
                "        </SubmitterFLUXParty>\n" +
                "        <SubjectMDRQueryIdentity>\n" +
                "            <ID schemeID=\"INDEX\">FA_REASON_DEPARTURE</ID>\n" +
                "        </SubjectMDRQueryIdentity>\n" +
                "    </ns3:MDRQuery>\n" +
                "</ns3:FLUXMDRQueryMessage>\n";
    }

}
