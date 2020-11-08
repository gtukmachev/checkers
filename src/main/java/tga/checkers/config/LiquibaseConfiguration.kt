package tga.checkers.config

import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.liquibase.SpringLiquibaseUtil
import liquibase.integration.spring.SpringLiquibase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import java.util.concurrent.Executor
import javax.sql.DataSource

@Configuration
class LiquibaseConfiguration(private val env: Environment) {
    private val log = LoggerFactory.getLogger(LiquibaseConfiguration::class.java)
    @Bean
    fun liquibase(@Qualifier("taskExecutor") executor: Executor?,
                  @LiquibaseDataSource liquibaseDataSource: ObjectProvider<DataSource?>, liquibaseProperties: LiquibaseProperties,
                  dataSource: ObjectProvider<DataSource?>, dataSourceProperties: DataSourceProperties?): SpringLiquibase {

        // If you don't want Liquibase to start asynchronously, substitute by this:
        // SpringLiquibase liquibase = SpringLiquibaseUtil.createSpringLiquibase(liquibaseDataSource.getIfAvailable(), liquibaseProperties, dataSource.getIfUnique(), dataSourceProperties);
        val liquibase: SpringLiquibase = SpringLiquibaseUtil.createAsyncSpringLiquibase(env, executor, liquibaseDataSource.ifAvailable, liquibaseProperties, dataSource.ifUnique, dataSourceProperties)
        liquibase.changeLog = "classpath:config/liquibase/master.xml"
        liquibase.contexts = liquibaseProperties.contexts
        liquibase.defaultSchema = liquibaseProperties.defaultSchema
        liquibase.liquibaseSchema = liquibaseProperties.liquibaseSchema
        liquibase.liquibaseTablespace = liquibaseProperties.liquibaseTablespace
        liquibase.databaseChangeLogLockTable = liquibaseProperties.databaseChangeLogLockTable
        liquibase.databaseChangeLogTable = liquibaseProperties.databaseChangeLogTable
        liquibase.isDropFirst = liquibaseProperties.isDropFirst
        liquibase.labels = liquibaseProperties.labels
        liquibase.setChangeLogParameters(liquibaseProperties.parameters)
        liquibase.setRollbackFile(liquibaseProperties.rollbackFile)
        liquibase.isTestRollbackOnUpdate = liquibaseProperties.isTestRollbackOnUpdate
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE))) {
            liquibase.setShouldRun(false)
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled)
            log.debug("Configuring Liquibase")
        }
        return liquibase
    }
}
