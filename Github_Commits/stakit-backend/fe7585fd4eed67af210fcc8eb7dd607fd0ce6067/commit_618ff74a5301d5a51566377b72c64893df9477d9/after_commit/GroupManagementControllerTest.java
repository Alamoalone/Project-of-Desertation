package dk.kvalitetsit.stakit.controller;

import dk.kvalitetsit.stakit.controller.exception.BadRequestException;
import dk.kvalitetsit.stakit.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.stakit.service.GroupService;
import dk.kvalitetsit.stakit.service.exception.InvalidDataException;
import dk.kvalitetsit.stakit.service.model.GroupGetModel;
import dk.kvalitetsit.stakit.service.model.GroupModel;
import dk.kvalitetsit.stakit.service.model.ServiceModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openapitools.model.GroupInput;
import org.openapitools.model.GroupPatch;
import org.openapitools.model.Service;
import org.springframework.http.HttpStatus;

import java.util.*;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

public class GroupManagementControllerTest {
    private GroupManagementController groupManagementController;
    private GroupService groupService;

    @Before
    public void setup() {
        groupService = Mockito.mock(GroupService.class);

        groupManagementController = new GroupManagementController(groupService);
    }

    @Test
    public void testGetGroups() {
        var groupOne = new GroupGetModel(UUID.randomUUID(), "Name 1", 20, new ArrayList<UUID>(), UUID.randomUUID().toString(),true, true);
        var groupTwo = new GroupGetModel(UUID.randomUUID(), "Name 2", 30, new ArrayList<UUID>(), UUID.randomUUID().toString(), true, true);

        Mockito.when(groupService.getGroups()).thenReturn(Arrays.asList(groupOne, groupTwo));

        var result = groupManagementController.v1GroupsGet();
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());

        var body = result.getBody();
        assertNotNull(body);
        assertEquals(2, body.size());

        assertEquals(groupOne.uuid(), body.get(0).getUuid());
        assertEquals(groupOne.name(), body.get(0).getName());
        assertEquals(groupOne.displayOrder(), body.get(0).getDisplayOrder());
        assertEquals(groupOne.services(), body.get(0).getServices());
        assertEquals(groupOne.description(), body.get(0).getDescription());

        assertEquals(groupTwo.uuid(), body.get(1).getUuid());
        assertEquals(groupTwo.name(), body.get(1).getName());
        assertEquals(groupTwo.displayOrder(), body.get(1).getDisplayOrder());
        assertEquals(groupTwo.services(), body.get(1).getServices());
        assertEquals(groupTwo.description(), body.get(1).getDescription());
    }

    @Test
    public void testInsertGroup() {
        var input = new GroupInput();
        input.setName("name");
        input.setDisplayOrder(10);
        input.setDescription("description");
        input.setServices(Collections.singletonList(UUID.randomUUID()));
        input.display(true);
        input.expanded(true);

        var expectedUuid = UUID.randomUUID();

        Mockito.when(groupService.createGroup(GroupModel.createInstance(input.getName(), input.getDisplayOrder(), input.getDescription(), input.getServices(), input.getDisplay(), input.getExpanded()))).thenReturn(expectedUuid);

        var result = groupManagementController.v1GroupsPost(input);
        assertNotNull(result);
        assertEquals(201, result.getStatusCode().value());
        assertEquals(expectedUuid.toString(), result.getHeaders().get("location").stream().findFirst().get());
        assertEquals(expectedUuid, result.getBody().getUuid());
    }

    @Test
    public void testUpdateGroup() {
        var uuid = UUID.randomUUID();
        var input = new GroupInput();
        input.setName("name");
        input.setDisplayOrder(10);
        input.setDescription("description");
        input.setServices(Collections.singletonList(UUID.randomUUID()));
        input.display(true);
        input.expanded(true);

        var serviceInput = new GroupModel(uuid, input.getName(), input.getDisplayOrder(), input.getDescription(), input.getServices(), input.getDisplay(), input.getExpanded());

        Mockito.when(groupService.updateGroup(serviceInput)).thenReturn(true);

        var result = groupManagementController.v1GroupsUuidPut(uuid, input);
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());

        Mockito.verify(groupService, times(1)).updateGroup(serviceInput);
    }

    @Test
    public void testUpdateGroupNotFound() {
        var uuid = UUID.randomUUID();
        var input = new GroupInput();
        input.setName("name");
        input.setDisplayOrder(10);
        input.setDescription("description");
        input.display(true);
        input.expanded(true);

        var serviceInput = new GroupModel(uuid, input.getName(), input.getDisplayOrder(), input.getDescription(), Collections.emptyList(), input.getDisplay(), input.getExpanded());

        Mockito.when(groupService.updateGroup(serviceInput)).thenReturn(false);

        var expectedException = assertThrows(ResourceNotFoundException.class, () -> groupManagementController.v1GroupsUuidPut(uuid, input));
        assertNotNull(expectedException);
        assertEquals("Group with uuid %s not found".formatted(uuid), expectedException.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, expectedException.getHttpStatus());

        Mockito.verify(groupService, times(1)).updateGroup(serviceInput);
    }

    @Test
    public void testDelete() {
        var uuid = UUID.randomUUID();

        Mockito.when(groupService.deleteGroup(uuid)).thenReturn(true);

        var result = groupManagementController.v1GroupsUuidDelete(uuid);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        Mockito.verify(groupService, times(1)).deleteGroup(uuid);
    }

    @Test
    public void testDeleteNotFound() {
        var uuid = UUID.randomUUID();

        Mockito.when(groupService.deleteGroup(uuid)).thenReturn(false);

        var expectedException = assertThrows(ResourceNotFoundException.class, () -> groupManagementController.v1GroupsUuidDelete(uuid));
        assertNotNull(expectedException);

        Mockito.verify(groupService, times(1)).deleteGroup(uuid);
    }

    @Test
    public void testGetGroup() {
        var uuid = UUID.randomUUID();
        var serviceUuid = UUID.randomUUID();

        Mockito.when(groupService.getGroup(uuid)).thenReturn(Optional.of(new GroupGetModel(uuid, "name", 10, Collections.singletonList(serviceUuid), "description", true, true)));

        var result = groupManagementController.v1GroupsUuidGet(uuid);
        assertNotNull(result);
        assertNotNull(result.getBody());

        assertEquals(uuid, result.getBody().getUuid());
        assertEquals("name", result.getBody().getName());
        assertEquals(10, result.getBody().getDisplayOrder());
        assertEquals(1, result.getBody().getServices().size());
        assertEquals(serviceUuid, result.getBody().getServices().get(0));
        assertEquals("description", result.getBody().getDescription());
    }

    @Test
    public void testGetGroupNotFound() {
        var uuid = UUID.randomUUID();

        Mockito.when(groupService.getGroup(uuid)).thenReturn(Optional.empty());

        var expectedException = assertThrows(ResourceNotFoundException.class, () -> groupManagementController.v1GroupsUuidGet(uuid));
        assertNotNull(expectedException);

        Mockito.verify(groupService, times(1)).getGroup(uuid);
    }

    @Test
    public void testPatchGroup(){
        var uuidGroup = UUID.randomUUID();
        var uuidService1 = UUID.randomUUID();
        var uuidService2 = UUID.randomUUID();

        var input = new GroupPatch();
        input.addServicesItem(uuidService1);
        input.addServicesItem(uuidService2);

        Mockito.when(groupService.patchGroup(uuidGroup, input.getServices())).thenReturn(true);

        var result = groupManagementController.v1GroupsUuidPatch(uuidGroup, input);
        assertNotNull(result);
        assertEquals(204, result.getStatusCodeValue());

        Mockito.verify(groupService, times(1)).patchGroup(uuidGroup, input.getServices());
    }

    @Test
    public void testPatchGroupNotFound() {
        var uuidGroup = UUID.randomUUID();
        var uuidService1 = UUID.randomUUID();
        var uuidService2 = UUID.randomUUID();

        var input = new GroupPatch();
        input.addServicesItem(uuidService1);
        input.addServicesItem(uuidService2);

        Mockito.when(groupService.patchGroup(uuidGroup, input.getServices())).thenReturn(false);

        var expectedException = assertThrows(ResourceNotFoundException.class, () -> groupManagementController.v1GroupsUuidPatch(uuidGroup, input));
        assertNotNull(expectedException);
        assertEquals("Group with uuid %s not found".formatted(uuidGroup), expectedException.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, expectedException.getHttpStatus());

        Mockito.verify(groupService, times(1)).patchGroup(uuidGroup, input.getServices());
    }

    @Test
    public void testPatchGroupServiceNotFound() {
        var uuidGroup = UUID.randomUUID();
        var uuidService1 = UUID.randomUUID();
        var uuidService2 = UUID.randomUUID();

        var input = new GroupPatch();
        input.addServicesItem(uuidService1);
        input.addServicesItem(uuidService2);


        Mockito.when(groupService.patchGroup(uuidGroup, input.getServices())).thenThrow(new InvalidDataException("message"));

        var result = assertThrows(BadRequestException.class, () -> groupManagementController.v1GroupsUuidPatch(uuidGroup, input));
        assertNotNull(result);
        assertEquals("message", result.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getHttpStatus());

        Mockito.verify(groupService, times(1)).patchGroup(uuidGroup, input.getServices());
    }

    @Test
    public void testGetServicesInGroup() {
        var input = UUID.randomUUID();
        var serviceModelList = new ArrayList<ServiceModel>();

        Mockito.when(groupService.getServicesInGroup(input)).thenReturn(Optional.of(serviceModelList));

        var service = new ServiceModel("serviceName", "serviceIdentifier", true, input, UUID.randomUUID(), "OK", "serviceDescription");
        serviceModelList.add(service);

        Mockito.when(groupService.getServicesInGroup(input)).thenReturn(Optional.of(serviceModelList));

        var result = groupManagementController.v1GroupsUuidServicesGet(input);
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1, result.getBody().size());

        assertEquals(service.name(), result.getBody().get(0).getName());
        assertEquals(service.serviceIdentifier(), result.getBody().get(0).getServiceIdentifier());
        assertEquals(service.ignoreServiceName(), result.getBody().get(0).getIgnoreServiceName());
        assertEquals(service.group(), result.getBody().get(0).getGroup());
        assertEquals(service.uuid(), result.getBody().get(0).getUuid());
        assertEquals(Service.StatusEnum.valueOf(service.status()), result.getBody().get(0).getStatus());
        assertEquals(service.description(), result.getBody().get(0).getDescription());

    }

    @Test
    public void testGetServicesInGroupNoServicesInGroup() {
        var input = UUID.randomUUID();
        var serviceModelList = new ArrayList<ServiceModel>();

        Mockito.when(groupService.getServicesInGroup(input)).thenReturn(Optional.of(serviceModelList));

        var result = groupManagementController.v1GroupsUuidServicesGet(input);
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals(0, result.getBody().size());
    }

    @Test
    public void testGetServicesInGroupNotFound() {
        var input = UUID.randomUUID();

        Mockito.when(groupService.getServicesInGroup(input)).thenReturn(Optional.empty());

        var expectedException = assertThrows(ResourceNotFoundException.class, () -> groupManagementController.v1GroupsUuidServicesGet(input));
        assertNotNull(expectedException);
        assertEquals("Group with uuid %s not found".formatted(input), expectedException.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, expectedException.getHttpStatus());
    }

    @Test
    public void testCreateGroupNullValue() {
        var input = new GroupInput();
        input.setName("name");
        input.setDisplayOrder(10);
        input.setDescription("description");
        input.setServices(Collections.singletonList(UUID.randomUUID()));
        input.display(null);
        input.setExpanded(null);

        var expectedUuid = UUID.randomUUID();

        Mockito.when(groupService.createGroup(GroupModel.createInstance(input.getName(), input.getDisplayOrder(), input.getDescription(), input.getServices(), true, false))).thenReturn(expectedUuid);

        var result = groupManagementController.v1GroupsPost(input);
        assertNotNull(result);
        assertEquals(201, result.getStatusCode().value());
        assertEquals(expectedUuid.toString(), result.getHeaders().get("location").stream().findFirst().get());
        assertEquals(expectedUuid, result.getBody().getUuid());
    }

    @Test
    public void testUpdateGroupNullValue() {
        var uuid = UUID.randomUUID();

        var input = new GroupInput();
        input.setName("name");
        input.setDisplayOrder(10);
        input.setDescription("description");
        input.setServices(Collections.singletonList(UUID.randomUUID()));
        input.display(null);
        input.setExpanded(null);

        Mockito.when(groupService.updateGroup(new GroupModel(uuid, input.getName(), input.getDisplayOrder(), input.getDescription(), input.getServices(), true, false))).thenReturn(true);

        var result = groupManagementController.v1GroupsUuidPut(uuid, input);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
    }
}
