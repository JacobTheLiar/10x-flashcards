import {Injectable} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {throwError} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiErrorHandlerService {
  constructor(private snackBar: MatSnackBar) {
  }

  /**
   * Metoda obsługująca błędy HTTP z API i wyświetlająca odpowiedni komunikat
   */
  handleError(error: HttpErrorResponse, defaultMessage: string = 'Wystąpił błąd podczas komunikacji z serwerem') {
    let errorMessage = defaultMessage;

    if (error.error instanceof ErrorEvent) {
      // Błąd klienta
      errorMessage = `Błąd: ${error.error.message}`;
    } else {
      // Błąd z serwera
      switch (error.status) {
        case 0:
          errorMessage = 'Brak połączenia z serwerem. Sprawdź połączenie internetowe.';
          break;
        case 400:
          errorMessage = this.parseServerErrorMessage(error) || 'Nieprawidłowe dane w zapytaniu';
          break;
        case 401:
          errorMessage = 'Brak uprawnień. Musisz się zalogować.';
          break;
        case 403:
          errorMessage = 'Brak dostępu do zasobu';
          break;
        case 404:
          errorMessage = 'Zasób nie został znaleziony';
          break;
        case 408:
          errorMessage = 'Przekroczono czas oczekiwania na odpowiedź serwera';
          break;
        case 500:
          errorMessage = 'Wewnętrzny błąd serwera. Spróbuj ponownie później.';
          break;
        default:
          errorMessage = this.parseServerErrorMessage(error) || defaultMessage;
      }
    }

    // Wyświetlenie komunikatu o błędzie
    this.showError(errorMessage);

    // Zwracamy Observable z błędem
    return throwError(() => new Error(errorMessage));
  }

  /**
   * Parsowanie komunikatu błędu z serwera
   */
  private parseServerErrorMessage(error: HttpErrorResponse): string | null {
    try {
      if (error.error?.message) {
        return error.error.message;
      }

      if (typeof error.error === 'string') {
        return error.error;
      }

      return null;
    } catch {
      return null;
    }
  }

  /**
   * Wyświetlenie komunikatu o błędzie
   */
  showError(message: string): void {
    this.snackBar.open(message, 'OK', {
      duration: 5000,
      panelClass: ['error-snackbar'],
      verticalPosition: 'top',
      horizontalPosition: 'center'
    });
  }

  /**
   * Wyświetlenie komunikatu o sukcesie
   */
  showSuccess(message: string): void {
    this.snackBar.open(message, 'OK', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }
}
