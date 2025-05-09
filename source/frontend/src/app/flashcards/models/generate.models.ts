// Model fiszki używany w widoku
export interface FlashcardViewModel {
  id?: string;
  frontContent: string;
  backContent: string;
  status: 'pending' | 'accepted' | 'rejected';
  sourceType: 'ai-full' | 'ai-edited' | 'manual';
  isEditing?: boolean;
  isSelected?: boolean;
}

// Model stanu komponentu generowania
export interface GenerateState {
  sourceText: string;
  isGenerating: boolean;
  error: string | null;
  generationId: string | null;
  flashcards: FlashcardViewModel[];
  isSaving: boolean;
  selectionMode: boolean;
}

// Konfiguracja komponentu wprowadzania tekstu
export interface TextInputConfig {
  value: string;
  maxLength: number;
  placeholder: string;
  disabled: boolean;
}

// Konfiguracja panelu akcji
export interface ActionPanelConfig {
  isGenerating: boolean;
  hasText: boolean;
  hasSuggestions: boolean;
  hasSelectedFlashcards: boolean;
  selectionMode: boolean;
}

// Stan edycji fiszki
export interface EditState {
  frontContent: string;
  backContent: string;
}
