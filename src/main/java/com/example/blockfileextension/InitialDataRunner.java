package com.example.blockfileextension;

import com.example.blockfileextension.domain.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class InitialDataRunner implements ApplicationRunner {

    private final BlockedFileExtensionRepository bfeRepository;
    private final ExtensionFrequencyRepository efRepository;

    public InitialDataRunner(BlockedFileExtensionRepository bfeRepository, ExtensionFrequencyRepository efRepository) {
        this.bfeRepository = bfeRepository;
        this.efRepository = efRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 데이터가 이미 존재하는지 확인
        if (bfeRepository.count() > 0) {
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
            bfeRepository.save(fixedExtension);
        }

        if (efRepository.count() > 0) {
            return;
        }
        int i = 0;
        for (String extension : fixedExtensions) {
            ExtensionFrequency extensionFrequency = new ExtensionFrequency(
                    extension,
                    i++
            );
            efRepository.save(extensionFrequency);
        }
    }
}
