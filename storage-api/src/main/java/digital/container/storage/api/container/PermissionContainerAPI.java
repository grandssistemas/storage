package digital.container.storage.api.container;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import digital.container.service.container.PermissionContainerService;
import digital.container.service.storage.MessageStorage;
import digital.container.storage.api.ApiDocumentation;
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
    @ApiOperation(value = "permission-container", notes = ApiDocumentation.POST_PERMISSION_CONTAINER)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = PermissionContainer.class),
            @ApiResponse(code = 400, message = MessageStorage.YOU_ALREADY_HAVE_ACCESS_TO_THE_CONTAINER)
    })
    public ResponseEntity<Object> save(@RequestBody PermissionContainer permissionContainer) {
        if(!this.permissionContainerService.containerKeyValid(permissionContainer.getContainerKey())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionContainerService.save(permissionContainer));
        }

        return ResponseEntity.badRequest().body(new String(MessageStorage.YOU_ALREADY_HAVE_ACCESS_TO_THE_CONTAINER+":"+permissionContainer.getContainerKey()));
    }
}
