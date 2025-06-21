package com.ach.eisenhower;

import com.ach.eisenhower.entities.AttachedDocumentEntity;
import com.ach.eisenhower.entities.BoardEntity;
import com.ach.eisenhower.entities.EisenhowerUserEntity;
import com.ach.eisenhower.entities.NoteEntity;
import com.ach.eisenhower.repositories.AttachedDocumentRepository;
import com.ach.eisenhower.repositories.BoardRepository;
import com.ach.eisenhower.repositories.EisenhowerUserRepository;
import com.ach.eisenhower.repositories.NoteRepository;
import com.ach.eisenhower.services.AttachedDocumentService;
import com.ach.eisenhower.services.BoardService;
import com.ach.eisenhower.services.DatabaseCleanupService;
import com.ach.eisenhower.services.NoteService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseCleanupTest extends EisenhowerBaseTest {
    @Autowired
    private DatabaseCleanupService databaseCleanupService;

    @Autowired
    private EisenhowerUserRepository userRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private AttachedDocumentRepository attachedDocumentRepository;

    @Autowired
    private BoardService boardService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private AttachedDocumentService attachedDocumentService;

    @Test
    public void testInactiveUsersAreDeleted() {
        // Arrange
        var testData = createUserWithData();
        var twoYearsAgo = LocalDate.now().minusYears(2).minusDays(1);
        testData.user.setLastLoginDate(Date.from(twoYearsAgo.atStartOfDay().toInstant(ZoneOffset.UTC)));
        userRepository.save(testData.user);

        // Act
        databaseCleanupService.CleanupInactiveUsers();

        // Assert
        assertTrue(userRepository.findByEmail(testData.user.getEmail()).isEmpty());
        assertNull(boardRepository.findById(testData.board.getId()));
        assertNull(noteRepository.findById(testData.note.getId()));
        assertNull(attachedDocumentRepository.findByNoteId(testData.note.getId()));
    }

    @Test
    public void testActiveUsersAreNotDeleted() {
        // Arrange
        var testData = createUserWithData();

        // Act
        databaseCleanupService.CleanupInactiveUsers();

        // Assert
        assertFalse(userRepository.findByEmail(testData.user.getEmail()).isEmpty());
        assertNotNull(boardRepository.findById(testData.board.getId()));
        assertNotNull(noteRepository.findById(testData.note.getId()));
        assertNotNull(attachedDocumentRepository.findByNoteId(testData.note.getId()));
    }

    private TestUserData createUserWithData() {
        var user = userTestingUtils.getValidUser();
        var board = boardService.createBoard(user, RandomStringUtils.randomAlphabetic(8));
        var note = noteService.createNote(user.getId(), board.getId(), RandomStringUtils.randomAlphabetic(20), 0.5, 0.5);
        var content = new byte[200];
        new Random().nextBytes(content);
        attachedDocumentService.upsertAttachedDocument(note.getBoard().getUser().getId(), note.getId(), content);
        var attachedDocument = attachedDocumentService.getAttachedDocument(user.getId(), note.getId());

        return new TestUserData(user, board, note, attachedDocument);
    }

    private record TestUserData(EisenhowerUserEntity user, BoardEntity board, NoteEntity note, AttachedDocumentEntity attachedDocument) {}
}
