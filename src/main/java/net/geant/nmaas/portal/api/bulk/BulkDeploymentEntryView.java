package net.geant.nmaas.portal.api.bulk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BulkDeploymentEntryView {

    private Boolean successful;

    private Boolean created;

    private Map<String, String> details;

    private BulkType type;

}