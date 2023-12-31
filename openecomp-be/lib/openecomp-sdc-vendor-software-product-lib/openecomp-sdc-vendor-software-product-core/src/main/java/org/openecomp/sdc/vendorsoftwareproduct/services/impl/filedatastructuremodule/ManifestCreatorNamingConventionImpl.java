/*
 * Copyright © 2016-2017 European Support Limited
 * Modifications copyright (c) 2021 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.FileData.Type;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.HeatFileAnalyzer;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.ManifestCreator;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.AnalyzedZipHeatFiles;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.Constants;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManifestCreatorNamingConventionImpl implements ManifestCreator {

    protected static final Logger logger = LoggerFactory.getLogger(ManifestCreatorNamingConventionImpl.class);
    private static final String CLOUD_SPECIFIC_FIXED_KEY_WORD = "cloudtech";
    private static final String[][] CLOUD_SPECIFIC_KEY_WORDS = {
        {"k8s", "azure", "aws"},  // cloud specific technology
        {"charts", "day0", "configtemplate"} // cloud specific subtype
    };
    private static final String CONTROLLER_BLUEPRINT_ARCHIVE_FIXED_KEY_WORD = "CBA";
    private static final String HELM_KEY_WORD = "HELM";
    private static final String PM_DICTIONARY = "PM_DICTIONARY";

    @Override
    public Optional<ManifestContent> createManifest(VspDetails vspDetails, FilesDataStructure filesDataStructure) {
        if (Objects.isNull(filesDataStructure)) {
            return Optional.empty();
        }
        List<FileData> fileDataList = new ArrayList<>();
        addModulesToManifestFileDataList(filesDataStructure, fileDataList);
        addNestedToManifest(filesDataStructure, fileDataList);
        addArtifactsToManifestFileDataList(filesDataStructure, fileDataList);
        return Optional.of(createManifest(vspDetails, fileDataList));
    }

    @Override
    public Optional<ManifestContent> createManifestFromExisting(VspDetails vspDetails, FilesDataStructure filesDataStructure,
                                                                ManifestContent existingManifest) {
        if (Objects.isNull(filesDataStructure)) {
            return Optional.empty();
        }
        List<FileData> fileDataList = new ArrayList<>();
        addModulesToManifestFileDataList(filesDataStructure, fileDataList);
        addNestedToManifest(filesDataStructure, fileDataList);
        addArtifactsToManifestFileDataList(filesDataStructure, fileDataList, existingManifest);
        return Optional.of(createManifest(vspDetails, fileDataList));
    }

    private void addArtifactsToManifestFileDataList(FilesDataStructure filesDataStructure, List<FileData> fileDataList,
                                                    ManifestContent existingManifest) {
        Collection<String> forArtifacts = CollectionUtils.union(filesDataStructure.getArtifacts(), filesDataStructure.getUnassigned());
        if (CollectionUtils.isNotEmpty(forArtifacts)) {
            for (String artifact : forArtifacts) {
                List<FileData> manifestData = existingManifest.getData();
                if (isCloudSpecificArtifact(artifact, manifestData)) {
                    fileDataList.add(createBaseFileData(Type.CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT, artifact));
                } else if (isControllerBlueprintArchive(artifact, manifestData)) {
                    fileDataList.add(createBaseFileData(Type.CONTROLLER_BLUEPRINT_ARCHIVE, artifact));
                } else if (isHelm(artifact, manifestData)) {
                    fileDataList.add(createBaseFileData(Type.HELM, artifact));
                } else if (isPmDictionary(artifact, manifestData)) {
                    fileDataList.add(createBaseFileData(Type.PM_DICTIONARY, artifact));
                } else {
                    fileDataList.add(createBaseFileData(Type.OTHER, artifact));
                }
            }
        }
    }

    private boolean isPmDictionary(String artifact, List<FileData> data) {
        if (CollectionUtils.isNotEmpty(data) && data.stream()
            .anyMatch(fileData -> fileData.getFile().equals(artifact) && Type.PM_DICTIONARY.equals(fileData.getType()))) {
            return true;
        } else {
            return artifact.toUpperCase().contains(PM_DICTIONARY);
        }
    }

    private void addNestedToManifest(FilesDataStructure filesDataStructure, List<FileData> fileDataList) {
        if (CollectionUtils.isNotEmpty(filesDataStructure.getNested())) {
            for (String nested : filesDataStructure.getNested()) {
                fileDataList.add(createBaseFileData(Type.HEAT, nested));
            }
        }
    }

    @Override
    public Optional<ManifestContent> createManifest(VspDetails vspDetails, FileContentHandler fileContentHandler,
                                                    AnalyzedZipHeatFiles analyzedZipHeatFiles) {
        logger.info("Trying to generate manifest");
        if (Objects.isNull(fileContentHandler) || CollectionUtils.isEmpty(fileContentHandler.getFileList())) {
            logger.info("fileContentHandler or filesList is empty. ManifestContent will not be created");
            return Optional.empty();
        }
        Map<String, byte[]> files = fileContentHandler.getFiles();
        List<FileData> fileDataList = createFileDataListFromZipFiles(fileContentHandler, files, analyzedZipHeatFiles.getFilesNotEligbleForModules());
        ManifestContent manifestContent = createManifest(vspDetails, fileDataList);
        return Optional.of(manifestContent);
    }

    private ManifestContent createManifest(VspDetails vspDetails, List<FileData> fileDataList) {
        ManifestContent manifestContent = new ManifestContent();
        manifestContent.setName(vspDetails.getName());
        manifestContent.setDescription(vspDetails.getDescription());
        manifestContent.setVersion(vspDetails.getVersion() == null ? null : vspDetails.getVersion().toString());
        // vsp version, need to check in confluence
        manifestContent.setData(fileDataList);
        return manifestContent;
    }

    private List<FileData> createFileDataListFromZipFiles(FileContentHandler fileContentHandler, Map<String, byte[]> files,
                                                          Collection<String> filesNotEligibleForModules) {
        Set<String> processedFiles = new HashSet<>();
        List<FileData> fileDataList = new ArrayList<>();
        for (String fileName : files.keySet()) {
            if (processedFiles.contains(fileName)) {
                continue;
            }
            if (isFileBaseFile(fileName)) {
                fileDataList.add(createModuleFileData(fileName, true, processedFiles, fileContentHandler.getFileList(), fileDataList));
            } else if (isFileModuleFile(fileName, filesNotEligibleForModules)) {
                fileDataList.add(createModuleFileData(fileName, false, processedFiles, fileContentHandler.getFileList(), fileDataList));
            } else {
                if (HeatFileAnalyzer.isYamlFile(fileName)) {
                    fileDataList.add(createBasicFileData(fileName, Type.HEAT, null));
                } else if (HeatFileAnalyzer.isEnvFile(fileName)) {
                    fileDataList.add(createBasicFileData(fileName, Type.HEAT_ENV, null));
                } else {
                    fileDataList.add(createBasicFileData(fileName, Type.OTHER, null));
                }
            }
        }
        return fileDataList;
    }

    private boolean isFileModuleFile(String fileName, Collection<String> filesCannotBeModule) {
        return !filesCannotBeModule.contains(fileName);
    }

    @Override
    public boolean isFileBaseFile(String fileName) {
        return Pattern.matches(Constants.BASE_HEAT_REGEX, fileName) && !isVolFile(fileName);
    }

    protected boolean isCloudSpecificArtifact(String artifact, List<FileData> data) {
        if (CollectionUtils.isNotEmpty(data) && data.stream()
            .anyMatch(fileData -> fileData.getFile().equals(artifact) && Type.CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT.equals(fileData.getType()))) {
            return true;
        } else {
            if (artifact.contains(CLOUD_SPECIFIC_FIXED_KEY_WORD)) {
                for (final String[] cloudSpecificKeyWord : CLOUD_SPECIFIC_KEY_WORDS) {
                    if (Arrays.stream(cloudSpecificKeyWord).noneMatch(artifact::contains)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean isControllerBlueprintArchive(String artifact, List<FileData> data) {
        if (CollectionUtils.isNotEmpty(data) && data.stream()
            .anyMatch(fileData -> fileData.getFile().equals(artifact) && Type.CONTROLLER_BLUEPRINT_ARCHIVE.equals(fileData.getType()))) {
            return true;
        } else {
            return artifact.toUpperCase().contains(CONTROLLER_BLUEPRINT_ARCHIVE_FIXED_KEY_WORD);
        }
    }

    private boolean isHelm(String artifact, List<FileData> data) {
        if (CollectionUtils.isNotEmpty(data) && data.stream()
            .anyMatch(fileData -> fileData.getFile().equals(artifact) && Type.HELM.equals(fileData.getType()))) {
            return true;
        } else {
            return artifact.toUpperCase().contains(HELM_KEY_WORD);
        }
    }

    private void addArtifactsToManifestFileDataList(FilesDataStructure filesDataStructure, List<FileData> fileDataList) {
        Collection<String> forArtifacts = CollectionUtils.union(filesDataStructure.getArtifacts(), filesDataStructure.getUnassigned());
        if (CollectionUtils.isNotEmpty(forArtifacts)) {
            for (String artifact : forArtifacts) {
                if (isCloudSpecificArtifact(artifact, null)) {
                    fileDataList.add(createBaseFileData(Type.CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT, artifact));
                } else if (isControllerBlueprintArchive(artifact, null)) {
                    fileDataList.add(createBaseFileData(Type.CONTROLLER_BLUEPRINT_ARCHIVE, artifact));
                } else if (isHelm(artifact, null)) {
                    fileDataList.add(createBaseFileData(Type.HELM, artifact));
                } else if (isPmDictionary(artifact, null)) {
                    fileDataList.add(createBaseFileData(Type.PM_DICTIONARY, artifact));
                } else {
                    fileDataList.add(createBaseFileData(Type.OTHER, artifact));
                }
            }
        }
    }

    private void addModulesToManifestFileDataList(FilesDataStructure filesDataStructure, List<FileData> fileDataList) {
        if (CollectionUtils.isNotEmpty(filesDataStructure.getModules())) {
            for (Module module : filesDataStructure.getModules()) {
                Type type = module.getType();
                if (type == null) {
                    type = Type.HEAT;
                }
                FileData fileData = createBaseFileData(type, module.getYaml());
                fileData.setBase(module.getIsBase());
                addEnv(module, fileData);
                addVolume(module, fileData);
                fileDataList.add(fileData);
            }
        }
    }

    private void addEnv(Module module, FileData fileData) {
        if (Objects.nonNull(module.getEnv())) {
            FileData env = createBaseFileData(Type.HEAT_ENV, module.getEnv());
            fileData.addFileData(env);
        }
    }

    private void addVolume(Module module, FileData fileData) {
        String volModule = module.getVol();
        if (Objects.nonNull(volModule)) {
            FileData vol = createBaseFileData(Type.HEAT_VOL, volModule);
            if (Objects.nonNull(module.getVolEnv())) {
                vol.addFileData(createBaseFileData(Type.HEAT_ENV, module.getVolEnv()));
            }
            fileData.addFileData(vol);
        }
    }

    private FileData createBaseFileData(Type heat, String yaml) {
        FileData fileData = new FileData();
        fileData.setType(heat);
        fileData.setFile(yaml);
        return fileData;
    }

    private FileData createModuleFileData(String moduleFileName, boolean isBase, Set<String> processedFiles, Set<String> fileNames,
                                          List<FileData> fileDataList) {
        FileData moduleFileData = createBasicFileData(moduleFileName, Type.HEAT, isBase);
        Optional<String> volFile = fetchRelatedVolume(moduleFileName, fileNames);
        volFile.ifPresent(vol -> {
            markFileAsProcessed(vol, processedFiles);
            removeFromFileDataListIfAlreadyProcessed(fileDataList, vol);
            FileData volFileData = createBasicFileData(vol, Type.HEAT_VOL, null);
            Optional<String> envFile = fetchRelatedEnv(vol, fileNames);
            envFile.ifPresent(env -> {
                markFileAsProcessed(env, processedFiles);
                removeFromFileDataListIfAlreadyProcessed(fileDataList, env);
                FileData envFileData = createBasicFileData(env, Type.HEAT_ENV, null);
                volFileData.addFileData(envFileData);
            });
            moduleFileData.addFileData(volFileData);
        });
        Optional<String> envFile = fetchRelatedEnv(moduleFileName, fileNames);
        envFile.ifPresent(env -> {
            markFileAsProcessed(env, processedFiles);
            FileData envFileData = createBasicFileData(env, Type.HEAT_ENV, null);
            moduleFileData.addFileData(envFileData);
        });
        return moduleFileData;
    }

    private void removeFromFileDataListIfAlreadyProcessed(List<FileData> fileDataList, String vol) {
        fileDataList.removeIf(fileData -> fileData.getFile().equals(vol));
    }

    private FileData createBasicFileData(String fileName, Type type, Boolean isBase) {
        FileData fileData = new FileData();
        if (isBase != null) {
            fileData.setBase(isBase);
        }
        fileData.setType(type);
        fileData.setFile(fileName);
        return fileData;
    }

    private Optional<String> fetchRelatedEnv(String fileName, Set<String> fileNames) {
        String envFileName = fileName.substring(0, fileName.lastIndexOf(".")) + Constants.ENV_FILE_EXTENSION;
        return fileNames.contains(envFileName) ? Optional.of(envFileName) : Optional.empty();
    }

    private Optional<String> fetchRelatedVolume(String fileName, Set<String> fileNames) {
        String volFile1stExt = extractVolFileName(fileName, ".yaml");
        String volFile2ndExt = extractVolFileName(fileName, ".yml");
        if (fileNames.contains(volFile1stExt)) {
            return Optional.of(volFile1stExt);
        }
        if (fileNames.contains(volFile2ndExt)) {
            return Optional.of(volFile2ndExt);
        }
        return Optional.empty();
    }

    private String extractVolFileName(String fileName, String fileExt) {
        return fileName.substring(0, fileName.lastIndexOf(".")) + Constants.VOL_FILE_NAME_SUFFIX + fileExt;
    }

    private boolean isVolFile(String fileName) {
        return fileName.endsWith(Constants.VOL_FILE_NAME_SUFFIX + ".yaml") || fileName.endsWith(Constants.VOL_FILE_NAME_SUFFIX + ".yml");
    }

    private void markFileAsProcessed(String fileName, Set<String> processedFiles) {
        processedFiles.add(fileName);
    }
}
