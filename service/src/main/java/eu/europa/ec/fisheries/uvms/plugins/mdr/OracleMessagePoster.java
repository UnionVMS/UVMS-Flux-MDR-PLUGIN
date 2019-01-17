/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.uvms.plugins.mdr;

import eu.europa.ec.fisheries.uvms.plugins.mdr.oracle.BaseMdrPluginBean;
import eu.europa.ec.fisheries.uvms.plugins.mdr.oracle.dao.MdmOracleDao;
import java.sql.SQLException;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import lombok.extern.slf4j.Slf4j;

@Stateless
@LocalBean
@Slf4j
public class OracleMessagePoster extends BaseMdrPluginBean {

    @EJB
    private StartupBean startupBean;

    private boolean isActive;

    private MdmOracleDao mdmDao;

    @PostConstruct
    public void init(){
        initEntityManager();
        mdmDao = new MdmOracleDao(getEntityManager());
        setActive(startupBean.getIsOracleActive());
    }

    public String postMessageToOracleDb(String messageToPost, String frParam, String onParam) throws SQLException {
        System.out.println("Oracle JDBC Driver Registered!");
        return mdmDao.findCodeListByMdrQuery(messageToPost, frParam, onParam, ORACLE_MDMD_STORED_PROC);
    }

    public boolean isActive() {
        return isActive;
    }

    private void setActive(boolean active) {
        isActive = active;
    }
}