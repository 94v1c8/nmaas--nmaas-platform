package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class HelmCommandPreparationTest {

    private static final String NAMESPACE = "nmaas";
    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");
    private static final String CHART_ARCHIVE_NAME = "/home/nmaas/charts/testapp-0.0.1.tgz";
    private static final String CHART_NAME_WITH_REPO = "test-repo/testapp";
    private static final String CHART_VERSION = "0.0.1";
    private static final String CORRECT_HELM_INSTALL_COMMAND_FIRST_PART =
            "helm install --name " + DEPLOYMENT_ID.value() + " --namespace " + NAMESPACE;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_ARCHIVE_NAME;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_NAME_WITH_REPO;
    private static final String CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION =
            CORRECT_HELM_INSTALL_COMMAND_FIRST_PART + " " + CHART_NAME_WITH_REPO + " --version " + CHART_VERSION;
    private static final String CORRECT_HELM_DELETE_COMMAND = "helm delete --purge " + DEPLOYMENT_ID.value();
    private static final String CORRECT_HELM_STATUS_COMMAND = "helm status " + DEPLOYMENT_ID.value();
    private static final String CORRECT_HELM_UPGRADE_COMMAND =
            "helm upgrade " + DEPLOYMENT_ID.value() + " " + CHART_ARCHIVE_NAME;
    private static final String CORRECT_HELM_VERSION_COMMAND = "helm version";
    private static final String TLS = " --tls";

    @Test
    public void shouldConstructInstallCommandUsingLocalChartArchiveWithNoArgumentsWithDisabledTls() {
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, DEPLOYMENT_ID.value(), null, CHART_ARCHIVE_NAME, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE));
    }

    @Test
    public void shouldConstructInstallCommandUsingLocalChartArchiveWithNoArgumentsWithEnabledTls() {
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, DEPLOYMENT_ID.value(), null, CHART_ARCHIVE_NAME, true).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_LOCAL_CHART_ARCHIVE + TLS));
    }

    @Test
    public void shouldConstructInstallCommandUsingChartFromRepoWithNoArgumentsWithDisabledTls() {
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        DEPLOYMENT_ID.value(),
                        null,
                        CHART_NAME_WITH_REPO,
                        null, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO));
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        DEPLOYMENT_ID.value(),
                        null,
                        CHART_NAME_WITH_REPO,
                        CHART_VERSION, false).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION));
    }

    @Test
    public void shouldConstructInstallCommandUsingChartFromRepoWithNoArgumentsWithEnabledTls() {
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        DEPLOYMENT_ID.value(),
                        null,
                        CHART_NAME_WITH_REPO,
                        null, true).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO + TLS));
        assertThat(
                HelmInstallCommand.commandWithRepo(
                        NAMESPACE,
                        DEPLOYMENT_ID.value(),
                        null,
                        CHART_NAME_WITH_REPO,
                        CHART_VERSION, true).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND_USING_CHART_FROM_REPO_WITH_VERSION + TLS));
    }

    @Test
    public void shouldConstructInstallCommandWithArgumentsWithDisabledTls() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(HelmKServiceManager.HELM_INSTALL_OPTION_PERSISTENCE_NAME, "persistenceName");
        arguments.put(HelmKServiceManager.HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS, "storageClass");
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, DEPLOYMENT_ID.value(), arguments, CHART_ARCHIVE_NAME, false).asString(),
                allOf(containsString(CORRECT_HELM_INSTALL_COMMAND_FIRST_PART),
                        containsString("persistenceName"),
                        containsString("storageClass")));
    }

    @Test
    public void shouldConstructInstallCommandWithArgumentsWithEnabledTls() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(HelmKServiceManager.HELM_INSTALL_OPTION_PERSISTENCE_NAME, "persistenceName");
        arguments.put(HelmKServiceManager.HELM_INSTALL_OPTION_PERSISTENCE_STORAGE_CLASS, "storageClass");
        assertThat(HelmInstallCommand.commandWithArchive(NAMESPACE, DEPLOYMENT_ID.value(), arguments, CHART_ARCHIVE_NAME, true).asString(),
                allOf(containsString(CORRECT_HELM_INSTALL_COMMAND_FIRST_PART),
                        containsString("persistenceName"),
                        containsString("storageClass"),
                        containsString(TLS)));
    }

    @Test
    public void shouldConstructDeleteCommandWithDisabledTls() {
        assertThat(HelmDeleteCommand.command(DEPLOYMENT_ID.value(), false).asString(), equalTo(CORRECT_HELM_DELETE_COMMAND));
    }

    @Test
    public void shouldConstructDeleteCommandWithEnabledTls() {
        assertThat(HelmDeleteCommand.command(DEPLOYMENT_ID.value(), true).asString(), equalTo(CORRECT_HELM_DELETE_COMMAND + TLS));
    }

    @Test
    public void shouldConstructStatusCommandWithDisabledTls() {
        assertThat(HelmStatusCommand.command(DEPLOYMENT_ID.value(), false).asString(), equalTo(CORRECT_HELM_STATUS_COMMAND));
    }

    @Test
    public void shouldConstructStatusCommandWithEnabledTls() {
        assertThat(HelmStatusCommand.command(DEPLOYMENT_ID.value(), true).asString(), equalTo(CORRECT_HELM_STATUS_COMMAND + TLS));
    }

    @Test
    public void shouldConstructUpgradeCommandWithDisabledTls() {
        assertThat(HelmUpgradeCommand.commandWithArchive(DEPLOYMENT_ID.value(), CHART_ARCHIVE_NAME, false).asString(),
                equalTo(CORRECT_HELM_UPGRADE_COMMAND));
    }

    @Test
    public void shouldConstructUpgradeCommandWithEnabledTls() {
        assertThat(HelmUpgradeCommand.commandWithArchive(DEPLOYMENT_ID.value(), CHART_ARCHIVE_NAME, true).asString(),
                equalTo(CORRECT_HELM_UPGRADE_COMMAND + TLS));
    }

    @Test
    public void shouldConstructVersionCommandWithDisabledTls(){
        assertThat(HelmVersionCommand.command(false).asString(), equalTo(CORRECT_HELM_VERSION_COMMAND));
    }

    @Test
    public void shouldConstructVersionCommandWithEnabledTls(){
        assertThat(HelmVersionCommand.command(true).asString(), equalTo(CORRECT_HELM_VERSION_COMMAND + TLS));
    }

}
