package com.example.blockfileextension;

import com.example.blockfileextension.domain.BlockedFileExtension;
import com.example.blockfileextension.domain.BlockedFileExtensionRepository;
import com.example.blockfileextension.domain.ExtensionType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class InitialDataRunner implements ApplicationRunner {

    private final BlockedFileExtensionRepository repository;

    public InitialDataRunner(BlockedFileExtensionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 데이터가 이미 존재하는지 확인
        if (repository.count() > 0) {
            return; // 이미 데이터가 있으면 추가하지 않고 종료
        }

        // 고정 확장자 목록
        List<String> fixedExtensions = Arrays.asList("bat", "cmd", "com", "cpl", "exe", "scr", "js");

        // 데이터베이스에 저장
        for (String extension : fixedExtensions) {
            BlockedFileExtension fixedExtension = new BlockedFileExtension(
                    extension,
                    ExtensionType.FIX,
                    false
            );
            repository.save(fixedExtension);
        }
    }
}
