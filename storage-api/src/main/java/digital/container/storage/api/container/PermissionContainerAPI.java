package digital.container.storage.api.container;

import com.wordnik.swagger.annotations.ApiOperation;
import digital.container.service.container.PermissionContainerService;
import digital.container.service.storage.MessageStorage;
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
    @ApiOperation(value = "Cadastrar uma chave para permitir gravar dados no storage.")
    public ResponseEntity<Object> save(@RequestBody PermissionContainer permissionContainer) {
        if(!this.permissionContainerService.containerKeyValid(permissionContainer.getContainerKey())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionContainerService.save(permissionContainer));
        }

        return ResponseEntity.badRequest().body(new String(MessageStorage.YOU_ALREADY_HAVE_ACCESS_TO_THE_CONTAINER+":"+permissionContainer.getContainerKey()));
    }
}
