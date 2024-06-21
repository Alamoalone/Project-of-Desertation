package dk.kvalitetsit.stakit.controller.mapper;

import dk.kvalitetsit.stakit.service.model.GroupGetModel;
import dk.kvalitetsit.stakit.service.model.GroupModel;
import org.openapitools.model.Group;
import org.openapitools.model.GroupInput;
import org.openapitools.model.GroupPatch;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupMapper {
    public static GroupModel mapCreateGroup(GroupInput from) {
        return GroupModel.createInstance(from.getName(), from.getDisplayOrder(), from.getDescription(), from.getServices(), from.getDisplay() == null || from.getDisplay(), from.getExpanded() == null ? false : from.getExpanded());
    }

    public static GroupModel mapUpdateGroup(UUID uuid, GroupInput from) {
        return new GroupModel(uuid, from.getName(), from.getDisplayOrder(), from.getDescription(), from.getServices(), from.getDisplay() == null || from.getDisplay(), from.getExpanded() == null ? false : from.getExpanded());
    }

    public static List<org.openapitools.model.Group> mapGetGroups(List<GroupGetModel> from) {
        return from.stream().map(GroupMapper::mapGroup).collect(Collectors.toList());
    }

    public static Group mapGroup(GroupGetModel groupModel) {
        return new Group()
                .displayOrder(groupModel.displayOrder())
                .uuid(groupModel.uuid())
                .name(groupModel.name())
                .services(groupModel.services())
                .description(groupModel.description())
                .display(groupModel.display())
                .expanded(groupModel.expanded());
    }

    public static List<UUID> mapPatchGroup(GroupPatch patch){
        return patch.getServices();
    }
}
