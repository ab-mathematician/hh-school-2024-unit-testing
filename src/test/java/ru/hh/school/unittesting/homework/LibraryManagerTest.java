package ru.hh.school.unittesting.homework;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest{

  @Mock
  private NotificationService notificationService;
  @Mock
  private UserService userService;

  @InjectMocks
  private LibraryManager libraryManager;

  @Test
  public void borrowBook_NotActiveAccount_Test(){
    when(userService.isUserActive(anyString())).thenReturn(false);
    assertFalse(libraryManager.borrowBook("Book", "UserNotActive"));

    Mockito.verify(notificationService).notifyUser("UserNotActive", "Your account is not active.");
  }

  @Test
  public void borrowBook_NotAvailableCopies_Test(){
    when(userService.isUserActive(anyString())).thenReturn(true);
    assertFalse(libraryManager.borrowBook("BookNotFound", "User"));
  }

  @Test
  public void borrowBook_Success_Test(){
    libraryManager.addBook("Book", 1);
    when(userService.isUserActive(anyString())).thenReturn(true);
    assertEquals(1, libraryManager.getAvailableCopies("Book"));
    assertTrue(libraryManager.borrowBook("Book", "User"));
    assertEquals(0, libraryManager.getAvailableCopies("Book"));

    Mockito.verify(notificationService).notifyUser("User", "You have borrowed the book: Book");
  }

  @Test
  public void returnBook_BookWasNotBorrowed_Test(){
    assertFalse(libraryManager.returnBook("BookIsNotBorrowed", "User"));
  }

  @Test
  public void returnBook_BookIsNotUsers_Test(){
    libraryManager.addBook("BookIsBorrowed", 1);
    when(userService.isUserActive(anyString())).thenReturn(true);
    libraryManager.borrowBook("BookIsBorrowed", "User");
    assertFalse(libraryManager.returnBook("BookIsBorrowed", "UserAnother"));
  }

  @Test
  public void returnBook_Success_Test(){
    libraryManager.addBook("Book", 1);
    when(userService.isUserActive(anyString())).thenReturn(true);
    libraryManager.borrowBook("Book", "User");
    assertEquals(0, libraryManager.getAvailableCopies("Book"));
    assertTrue(libraryManager.returnBook("Book", "User"));
    assertEquals(1, libraryManager.getAvailableCopies("Book"));

    Mockito.verify(notificationService).notifyUser("User", "You have returned the book: Book");
  }


  @Test
  public void getAvailableCopies_NotFound_Test(){
    libraryManager.addBook("Book1", 4);
    assertEquals(4, libraryManager.getAvailableCopies("Book1"));
    assertEquals(0, libraryManager.getAvailableCopies("Book2"));
  }


  @Test
  public void calculateDynamicLateFee_OverdueDaysNegative_Test(){
    var exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-1, true, true)
    );
    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }

  @Test
  public void calculateDynamicLateFee_ZeroDays_Test(){
    assertEquals(0, libraryManager.calculateDynamicLateFee(0, true, true));
  }

  @ParameterizedTest
  @CsvSource({
      "500, 1000, False, False",
      "750, 1000, True, False",
      "400, 1000, False, True",
      "600, 1000, True, True",
      "1.2, 2, True, True", // rounding test
      "3.6, 6, True, True"  // rounding test
  })
  public void calculateDynamicLateFee_Calculation_Test(double feeValue, int daysCount, boolean isBestseller, boolean isPremiumMember){
    assertEquals(feeValue, libraryManager.calculateDynamicLateFee(daysCount, isBestseller, isPremiumMember));
  }
}