package digital.container.storage.api.container;

import digital.container.service.container.PermissionContainerService;
import digital.container.storage.domain.model.container.PermissionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/permission-container")
public class PermissionContainerAPI {

    @Autowired
    private PermissionContainerService permissionContainerService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> save(@RequestBody PermissionContainer permissionContainer) {
        if(!this.permissionContainerService.containerKeyValid(permissionContainer.getContainerKey())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionContainerService.save(permissionContainer));
        }
        return ResponseEntity.badRequest().body(new String("You already have access to container:"+permissionContainer.getContainerKey()));
    }
}
