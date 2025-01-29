package yandex.cloud.sdk.examples.compute;

import java.util.Map;

// Notate config.json
public class Config {
    private String folder_id;
    private String username;
    private Resources resources;
    private Map<String, String> metadata;
    private Map<String, String> labels;

    public String getFolderId() {
        return folder_id;
    }

    public String getUsername() {
        return username;
    }

    public Resources getResources() {
        return resources;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public static class Resources {
        private Image image;
        private String name;
        private ResourcesSpec resources_spec;
        private BootDiskSpec boot_disk_spec;
        private String zone_id;
        private String platform_id;
        private String subnet_id;

        public Image getImage() {
            return image;
        }

        public String getName() {
            return name;
        }

        public ResourcesSpec getResourcesSpec() {
            return resources_spec;
        }

        public BootDiskSpec getBootDiskSpec() {
            return boot_disk_spec;
        }

        public String getZoneId() {
            return zone_id;
        }

        public String getPlatformId() {
            return platform_id;
        }

        public String getSubnetId() {
            return subnet_id;
        }
    }

    public static class Image {
        private String family;
        private String folder_family_id;

        public String getFamily() {
            return family;
        }

        public String getFolderFamilyId() {
            return folder_family_id;
        }
    }

    public static class ResourcesSpec {
        private long memory;
        private int cores;

        public long getMemory() {
            return memory;
        }

        public int getCores() {
            return cores;
        }
    }

    public static class BootDiskSpec {
        private boolean auto_delete;
        private DiskSpec disk_spec;

        public boolean isAutoDelete() {
            return auto_delete;
        }

        public DiskSpec getDiskSpec() {
            return disk_spec;
        }
    }

    public static class DiskSpec {
        private String type_id;
        private long size;

        public String getTypeId() {
            return type_id;
        }

        public long getSize() {
            return size;
        }
    }
}
