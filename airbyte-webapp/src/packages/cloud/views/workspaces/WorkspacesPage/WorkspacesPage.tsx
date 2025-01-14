import React from "react";
import { FormattedMessage } from "react-intl";

import { Heading } from "components/ui/Heading";
import { Text } from "components/ui/Text";

import { useTrackPage, PageTrackingCodes } from "core/services/analytics";
import { useIntercom } from "packages/cloud/services/thirdParty/intercom";
import { useCreateCloudWorkspace } from "packages/cloud/services/workspaces/CloudWorkspacesService";

import logoUrl from "./components/workspaceHeaderLogo.svg";
import { WorkspacesControl } from "./components/WorkspacesControl";
import WorkspacesList from "./components/WorkspacesList";
import styles from "./WorkspacesPage.module.scss";

const WorkspacesPage: React.FC = () => {
  useTrackPage(PageTrackingCodes.WORKSPACES);
  useIntercom();
  const { mutateAsync: createCloudWorkspace } = useCreateCloudWorkspace();

  return (
    <div className={styles.container}>
      <img className={styles.logo} alt="" src={logoUrl} width={186} />
      <Heading as="h1" size="lg" centered>
        <FormattedMessage id="workspaces.title" />
      </Heading>
      <Text align="center" className={styles.subtitle}>
        <FormattedMessage id="workspaces.subtitle" />
      </Text>
      <WorkspacesList />
      <WorkspacesControl onSubmit={createCloudWorkspace} />
    </div>
  );
};

export default WorkspacesPage;
