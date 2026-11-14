import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Stagiaire } from './stagiaire';

describe('Stagiaire', () => {
  let service: Stagiaire;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Stagiaire);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
