package dk.kvalitetsit.stakit.integrationtest;

import org.junit.Test;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.GroupManagementApi;
import org.openapitools.client.api.ServiceManagementApi;
import org.openapitools.client.model.GroupInput;
import org.openapitools.client.model.GroupPatch;
import org.openapitools.client.model.ServiceCreate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GroupManagementIT extends AbstractIntegrationTest {
    private final GroupManagementApi groupManagementApi;
    private final ServiceManagementApi serviceManagementApi;

    public GroupManagementIT() throws NoSuchAlgorithmException, InvalidKeySpecException, URISyntaxException, IOException {
        var apiClient = new ApiClient();
        apiClient.setBasePath(getApiBasePath());
        apiClient.addDefaultHeader("Authorization", "Bearer " + generateSignedToken());

        groupManagementApi = new GroupManagementApi(apiClient);
        serviceManagementApi = new ServiceManagementApi(apiClient);
    }

    @Test
    public void testCreateUpdateAndGetGroup() throws ApiException {
        var groupUpdate = new GroupInput();
        groupUpdate.setName("name");
        groupUpdate.setDisplayOrder(20);
        groupUpdate.setDescription("description");
        groupUpdate.setDisplay(true);
        groupUpdate.setExpanded(true);
        var response = groupManagementApi.v1GroupsPostWithHttpInfo(groupUpdate);
        assertEquals(201, response.getStatusCode());

        var uuid = response.getData().getUuid();
        assertEquals(uuid.toString(), response.getHeaders().get("Location").get(0));

        groupUpdate.setDisplayOrder(10);
        groupUpdate.setName("name updated");
        groupUpdate.setDescription("description updated");
        groupManagementApi.v1GroupsUuidPut(uuid, groupUpdate);

        var result = groupManagementApi.v1GroupsGet();
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(x -> groupUpdate.getDisplayOrder().equals(x.getDisplayOrder()) &&
               groupUpdate.getName().equals(x.getName()) && groupUpdate.getDescription().equals(x.getDescription())));
    }

    @Test
    public void testCreateUpdateAndGetGroupWithServices() throws ApiException {
        var serviceInput = new ServiceCreate()
                .ignoreServiceName(false)
                .serviceIdentifier(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString());

        var service = serviceManagementApi.v1ServicesPost(serviceInput);

        var groupUpdate = new GroupInput();
        groupUpdate.setName("name");
        groupUpdate.setDisplayOrder(20);
        groupUpdate.setDescription("description");

        groupUpdate.expanded(true);
        groupUpdate.display(true);
        var response = groupManagementApi.v1GroupsPostWithHttpInfo(groupUpdate);
        assertEquals(201, response.getStatusCode());

        var uuid = response.getData().getUuid();
        assertEquals(uuid.toString(), response.getHeaders().get("Location").get(0));

        groupUpdate.setDisplayOrder(10);
        groupUpdate.setName("name updated");
        groupUpdate.setDescription("description updated");
        groupUpdate.addServicesItem(service.getUuid());
        groupManagementApi.v1GroupsUuidPut(uuid, groupUpdate);

        var result = groupManagementApi.v1GroupsGet();
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(x -> groupUpdate.getDisplayOrder().equals(x.getDisplayOrder()) &&
                groupUpdate.getName().equals(x.getName()) &&
                groupUpdate.getServices().stream().allMatch(s -> s.equals(service.getUuid())) &&
                groupUpdate.getDescription().equals(x.getDescription())));
    }

    @Test
    public void testCreateGetGroupWithServices() throws ApiException {
        var serviceInput = new ServiceCreate()
                .ignoreServiceName(false)
                .serviceIdentifier(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString());

        var service = serviceManagementApi.v1ServicesPost(serviceInput);

        var groupUpdate = new GroupInput();
        groupUpdate.setName("name");
        groupUpdate.setDisplayOrder(20);
        groupUpdate.setDescription("description");
        groupUpdate.addServicesItem(service.getUuid());
        groupUpdate.setDisplay(true);
        groupUpdate.setExpanded(true);
        var response = groupManagementApi.v1GroupsPostWithHttpInfo(groupUpdate);
        assertEquals(201, response.getStatusCode());

        var result = groupManagementApi.v1GroupsGet();
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(x -> groupUpdate.getDisplayOrder().equals(x.getDisplayOrder()) &&
                groupUpdate.getName().equals(x.getName()) &&
                groupUpdate.getServices().stream().allMatch(s -> s.equals(service.getUuid())) &&
                groupUpdate.getDescription().equals(x.getDescription())));
    }

    @Test
    public void testGetSingleGroup() throws ApiException {
        var groupUpdate = new GroupInput();
        groupUpdate.setName("name");
        groupUpdate.setDisplayOrder(20);
        groupUpdate.setDescription("description");
        groupUpdate.display(true);

        groupUpdate.setExpanded(true);
        var response = groupManagementApi.v1GroupsPost(groupUpdate);

        var result = groupManagementApi.v1GroupsUuidGet(response.getUuid());
        assertNotNull(result);
        assertEquals(groupUpdate.getName(), result.getName());
        assertEquals(groupUpdate.getDisplayOrder(), result.getDisplayOrder());
        assertEquals(response.getUuid(), result.getUuid());
        assertEquals(groupUpdate.getDescription(), result.getDescription());
    }

    @Test
    public void testDeleteGroup() throws ApiException {
        var groupUpdate = new GroupInput();
        groupUpdate.setName("name");
        groupUpdate.setDisplayOrder(20);
        groupUpdate.setDescription("description");
        groupUpdate.display(true);
        groupUpdate.expanded(true);
        var createResponse = groupManagementApi.v1GroupsPostWithHttpInfo(groupUpdate);
        var uuid = createResponse.getData().getUuid();

        var response = groupManagementApi.v1GroupsUuidDeleteWithHttpInfo(uuid);
        assertEquals(204, response.getStatusCode());
    }

    @Test
    public void testGetAll() throws ApiException {
        var groups = groupManagementApi.v1GroupsGet();

        assertNotNull(groups);

        assertTrue(groups.size() > 0);
        assertTrue(groups.stream().anyMatch(x -> x.getName().equals("Default")));
    }

    @Test
    public void testPatchGroup() throws ApiException {
        var group = new GroupInput();
        group.setName(UUID.randomUUID().toString());
        group.setDisplayOrder(10);
        group.setDescription("group patch description");
        group.display(true);

        group.setExpanded(true);
        var groupToPatch = groupManagementApi.v1GroupsPost(group);

        var groupUuid = groupToPatch.getUuid();

        var input1 = new ServiceCreate();
        input1.setServiceIdentifier("patch service1");
        input1.setName("patch name1");
        input1.setIgnoreServiceName(true);
        input1.setGroup(groupUuid);
        input1.setDescription("patch description1");
        var service1 = serviceManagementApi.v1ServicesPost(input1);

        var input2 = new ServiceCreate();
        input2.setServiceIdentifier("patch service2");
        input2.setName("patch name2");
        input2.setIgnoreServiceName(true);
        input2.setGroup(groupUuid);
        input2.setDescription("patch description2");
        var service2 = serviceManagementApi.v1ServicesPost(input2);

        var input3 = new ServiceCreate();
        input3.setServiceIdentifier("patch service3");
        input3.setName("patch name3");
        input3.setIgnoreServiceName(true);
        input3.setGroup(null);
        input3.setDescription("patch description3");
        var service3 = serviceManagementApi.v1ServicesPost(input3);

        var patch = new GroupPatch();

        patch.addServicesItem(service2.getUuid());
        patch.addServicesItem(service3.getUuid());

        var response = groupManagementApi.v1GroupsUuidPatchWithHttpInfo(groupUuid, patch);
        assertEquals(204, response.getStatusCode());

        var patchedGroup = groupManagementApi.v1GroupsUuidGet(groupUuid);
        assertEquals(group.getName(), patchedGroup.getName());
        assertEquals(group.getDisplayOrder(), patchedGroup.getDisplayOrder());
        assertEquals(group.getDescription(), patchedGroup.getDescription());

        var services = patchedGroup.getServices();
        assertNotNull(services);
        assertEquals(2, services.size());
        assertFalse(services.stream().anyMatch(x ->  x.equals(service1.getUuid())));
        assertTrue(services.stream().anyMatch(x -> x.equals(service2.getUuid())));
        assertTrue(services.stream().anyMatch(x -> x.equals(service3.getUuid())));
    }

    @Test
    public void testGetServicesInGroup() throws ApiException {
        var group = new GroupInput();
        group.setName("group with services");
        group.setDisplayOrder(10);
        group.setDescription("group with services description");
        group.display(true);

        group.setExpanded(true);
        var createResponse = groupManagementApi.v1GroupsPost(group);

        var uuid = createResponse.getUuid();

        var input1 = new ServiceCreate();
        input1.setServiceIdentifier("get service1");
        input1.setName("get name1");
        input1.setIgnoreServiceName(true);
        input1.setGroup(uuid);
        input1.setDescription("get description1");
        var service1 = serviceManagementApi.v1ServicesPost(input1);

        var input2 = new ServiceCreate();
        input2.setServiceIdentifier("get service2");
        input2.setName("get name2");
        input2.setIgnoreServiceName(true);
        input2.setGroup(uuid);
        input2.setDescription("get description2");
        var service2 = serviceManagementApi.v1ServicesPost(input2);

        var response = groupManagementApi.v1GroupsUuidServicesGetWithHttpInfo(uuid);
        assertEquals(200, response.getStatusCode());
        assertEquals(2, response.getData().size());

        var serv1 = response.getData().get(0);
        assertEquals(service1.getUuid(), serv1.getUuid());
        assertEquals(input1.getName(), serv1.getName());
        assertEquals(input1.getServiceIdentifier(), serv1.getServiceIdentifier());
        assertEquals(input1.getGroup(), serv1.getGroup());
        assertEquals(input1.getDescription(), serv1.getDescription());

        var serv2 = response.getData().get(1);
        assertEquals(service2.getUuid(), serv2.getUuid());
        assertEquals(input2.getName(), serv2.getName());
        assertEquals(input2.getServiceIdentifier(), serv2.getServiceIdentifier());
        assertEquals(input2.getGroup(), serv2.getGroup());
        assertEquals(input2.getDescription(), serv2.getDescription());
    }

    @Test
    public void testCreateAndGetGroupNullDisplay() throws ApiException {
        var input = new GroupInput();
        input.setName("name");
        input.setDisplayOrder(20);
        input.setDescription("description");
        var response = groupManagementApi.v1GroupsPostWithHttpInfo(input);
        assertEquals(201, response.getStatusCode());

        var uuid = response.getData().getUuid();
        assertEquals(uuid.toString(), response.getHeaders().get("Location").get(0));

        var result = groupManagementApi.v1GroupsGet();
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(x ->
                input.getDisplayOrder().equals(x.getDisplayOrder()) &&
                input.getName().equals(x.getName()) &&
                input.getDescription().equals(x.getDescription()) &&
                x.getDisplay()));
    }
}
