If you dont have access to a public nexus/mvn repo with this archetype you can release it locally and create your modules from this archetype

1. In the archetyperoot ( Where this readme is located ), open cmd and type [ mvn clean archetype:create-from-project ]
2. cd target/generated-sources/archetype ( from the archetype root )
3. Type [ mvn install ]

Now your archetype is released to your local .m2 repository (eu.europa.ec.fisheries.uvms.component.component-archetype)

To create a project from archetype do as follows

1. Create a new folder where you want the project to be
2. open cmd and cd to that folder
3. type [ mvn archetype:generate -DarchetypeCatalog=local ]
4. You will be presented with options from your local artifact repo. Chose the one that have the namespace  "eu.europa.ec.fisheries.uvms.component:component-archetype"
5. Define value for property 'groupId': : eu.europa.ec.fisheries.uvms.plugins.flux.mdrs.YOUR_COMPONENT_NAME
6. Define value for property 'artifactId': : YOUR_COMPONENT_NAME
7. Define value for property 'version':  1.0-SNAPSHOT: : 1.0.0-SNAPSHOT
8. Define value for property 'package':  eu.europa.ec.fisheries.uvms.plugins.flux.mdrs.YOUR_COMPONENT_NAME: : eu.europa.ec.fisheries.uvms.plugins.flux.mdr.YOUR_COMPONENT_NAME
9. Select Y and Enter and you're done!
10. Rename the folder YOUR_COMPONENT_NAME to APP.

Open the generated component in your ide and mvn clean build to ensure that the component is correctly configured

++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
+++++++++++++++++++++++++++++++ ONLY FOR COMMISSION DEPLOYMENT CONFIG PART +++++++++++++++++++++++++++++++++++++++
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

IMPORTANT NOTE : For commission deployment some preparation must be done before deployment is possible.
                 Take the module from following branch : "feature/mdm_oracle_call_implementation" and then configure
                 the datasource related to Oracle db (MDM).
                 This because for commission deployment this plugin doesn't exchange jms messages with flux, instead,
                 it uses Oracle store_procedures to get the code lists.
                 Follow the steps below :

Step 1 : In wildfly instance create the following folder structure "com/oracle/main/" under modules folder, (usually /opt/jboss/wildfly/modules/com/oracle/main/)
         And put there :
            1. Oracle driver (usually ojdbc6.jar)
            2. Create module.xml with the following content (and put it in the same location of the driver) :

        module.xml :

        <?xml version="1.0" encoding="UTF-8"?>
        <module xmlns="urn:jboss:module:1.0" name="com.oracle">
            <resources>
                <resource-root path="ojdbc.jar"/>
            </resources>
            <dependencies>
                <module name="javax.api"/>
                <module name="javax.transaction.api"/>
            </dependencies>
        </module>

Step 2 : Create datasource definition.

                XA Datasource :

                <xa-datasource jta="true" jndi-name="java:/jdbc/uvms_mdm_oracle" pool-name="uvms_mdm_oracle" enabled="true" use-ccm="true" statistics-enabled="false">
                    <driver>ojdbc</driver>
                    <xa-datasource-class>oracle.jdbc.xa.client.OracleXADataSource</xa-datasource-class>
                    <xa-datasource-property name="URL">jdbc:oracle:thin:@[[[[ip_of_oracle_db_deployment]]]]:1521:xe</xa-datasource-property>
                    <xa-pool>
                        <min-pool-size>2</min-pool-size>
                        <initial-pool-size>2</initial-pool-size>
                        <max-pool-size>20</max-pool-size>
                        <prefill>true</prefill>
                    </xa-pool>
                    <validation>
                        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter"></exception-sorter>
                    </validation>
                    <security>
                        <user-name>flux_msg</user-name>
                        <password>password</password>
                    </security>
                </xa-datasource>

                or "Normal" Datasource :

                <datasource jta="true" jndi-name="java:/jdbc/uvms_mdm_oracle" pool-name="uvms_mdm_oracle" enabled="true" use-ccm="true" statistics-enabled="false">
                    <connection-url>jdbc:oracle:thin:@172.18.0.4:1521:xe</connection-url>
                    <driver>oraclesql</driver>
                    <security>
                        <user-name>flux_msg</user-name>
                        <password>password</password>
                    </security>
                    <pool>
                        <min-pool-size>2</min-pool-size>
                        <initial-pool-size>2</initial-pool-size>
                        <max-pool-size>20</max-pool-size>
                        <prefill>true</prefill>
                    </pool>
                </datasource>

Step 3 : persistence.xml configuration (must be present in the plugin under : ..\service\src\main\resources\META-INF) :


        <persistence-unit name="mdrPluginPUOracle" transaction-type="JTA">
            	<provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
                <jta-data-source>java:/jdbc/uvms_mdm_oracle</jta-data-source>
                <properties>
                    <property name="hibernate.dialect" value="org.hibernate.dialect.Oracle10gDialect" />
                    <property name="hibernate.query.substitutions" value="true 1, false 0" />
                    <property name="hibernate.hbm2ddl.auto" value="none"/>
                    <property name="show_sql" value="false"/>
                    <property name="format_sql" value="true"/>
                    <property name="use_sql_comments" value="false"/>
                    <property name="hibernate.cache.use_second_level_cache" value="true"/>
                </properties>
        </persistence-unit>

You're all setup. You can compile and the deployment can be done now.