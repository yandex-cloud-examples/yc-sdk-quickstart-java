package yandex.cloud.sdk.examples.compute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import yandex.cloud.api.compute.v1.ImageOuterClass.Image;
import yandex.cloud.api.compute.v1.ImageServiceGrpc;
import yandex.cloud.api.compute.v1.ImageServiceGrpc.ImageServiceBlockingStub;
import yandex.cloud.api.compute.v1.ImageServiceOuterClass.GetImageLatestByFamilyRequest;
import yandex.cloud.api.compute.v1.InstanceServiceGrpc;
import yandex.cloud.api.compute.v1.InstanceServiceGrpc.InstanceServiceBlockingStub;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.AttachedDiskSpec;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.AttachedDiskSpec.DiskSpec;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.CreateInstanceMetadata;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.CreateInstanceRequest;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.NetworkInterfaceSpec;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.PrimaryAddressSpec;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.ResourcesSpec;
import yandex.cloud.api.operation.OperationOuterClass.Operation;
import yandex.cloud.api.operation.OperationServiceGrpc;
import yandex.cloud.api.operation.OperationServiceGrpc.OperationServiceBlockingStub;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;
import yandex.cloud.sdk.utils.OperationUtils;

// Entry point of the ComputeExample.java program
public class ComputeExample {
    // Declare and initialize static final variables
    private static final Config config = ConfigLoader.main();
    private static final String IAM_TOKEN = System.getenv("IAM_TOKEN");
    private static final String SSH_PUBLIC_KEY_PATH = System.getenv("SSH_PUBLIC_KEY_PATH");
    private static String SSH_PUBLIC_KEY;

    public static void main(String[] args) throws Exception {
        // Authorization using IAM token
        ServiceFactory factory = ServiceFactory.builder()
            .credentialProvider(Auth.iamTokenBuilder().token(IAM_TOKEN))
            .requestTimeout(Duration.ofMinutes(1))
            .build();
        // Read and set the value of the public SSH key
        SSH_PUBLIC_KEY = readSSHKey(SSH_PUBLIC_KEY_PATH);
        InstanceServiceBlockingStub instanceService = factory.create(InstanceServiceBlockingStub.class, InstanceServiceGrpc::newBlockingStub);
        OperationServiceBlockingStub operationService = factory.create(OperationServiceBlockingStub.class, OperationServiceGrpc::newBlockingStub);
        ImageServiceBlockingStub imageService = factory.create(ImageServiceBlockingStub.class, ImageServiceGrpc::newBlockingStub);

        // Get the image for the system
        Image image = imageService.getLatestByFamily(buildGetLatestByFamilyRequest());
        
        // Create VM
        Operation createOperation = instanceService.create(buildCreateInstanceRequest(image.getId()));
        String operationId = createOperation.getId();
        System.out.println(String.format("Running Yandex.Cloud operation. ID: %s", operationId));

        // Wait for the operation result
        String instanceId = createOperation.getMetadata().unpack(CreateInstanceMetadata.class).getInstanceId();
        OperationUtils.wait(operationService, createOperation, Duration.ofMinutes(5));
        // Output the ID of the created VM to the console
        System.out.println(String.format("Instance with id %s was created", instanceId));
    }

    // Get information about the selected system image
    private static GetImageLatestByFamilyRequest buildGetLatestByFamilyRequest() {
        Config.Image imageConfig = config.getResources().getImage();
        return GetImageLatestByFamilyRequest.newBuilder()
                .setFolderId(imageConfig.getFolderFamilyId())
                .setFamily(imageConfig.getFamily())
                .build();
    }

    // Function to set VM configurations
    private static CreateInstanceRequest buildCreateInstanceRequest(String imageId) {
        // Get resources from the configuration
        Config.Resources resources = config.getResources();
    
        return CreateInstanceRequest.newBuilder()
            .setFolderId(config.getFolderId()) // Set the folder ID
            .setName(resources.getName()) // VM name
            .setZoneId(resources.getZoneId()) // Set the availability zone ID
            .setPlatformId(resources.getPlatformId()) // Set the platform ID
            .setResourcesSpec(ResourcesSpec.newBuilder()
                .setCores(resources.getResourcesSpec().getCores()) // Set the number of CPUs
                .setMemory(resources.getResourcesSpec().getMemory())) // Set RAM
            .setBootDiskSpec(AttachedDiskSpec.newBuilder()
                .setDiskSpec(DiskSpec.newBuilder() // Define the disk configuration
                    .setImageId(imageId) // Set the image ID
                    .setSize(resources.getBootDiskSpec().getDiskSpec().getSize())) // Set the disk size
                .setAutoDelete(resources.getBootDiskSpec().isAutoDelete())) // Set auto-delete
            .addNetworkInterfaceSpecs(NetworkInterfaceSpec.newBuilder()
                .setSubnetId(resources.getSubnetId()) // Set the subnet ID
                .setPrimaryV4AddressSpec(PrimaryAddressSpec.getDefaultInstance())
            )
            .putAllMetadata(processMetadata(config)) // Set the value of the metadata field
            .putAllLabels(config.getLabels()) // Set VM labels
            .build();
    }
    

    // Function to read the public part of the SSH key from the specified path
    public static String readSSHKey(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    // Function to update the values of USERNAME and SSH_PUBLIC_KEY within the metadata
    public static Map<String, String> processMetadata(Config config) {
        Map<String, String> metadata = config.getMetadata();
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue()
                .replace("USERNAME", config.getUsername())
                .replace("SSH_PUBLIC_KEY", SSH_PUBLIC_KEY);
            result.put(key, value);
        }

        return result;
    }
}

