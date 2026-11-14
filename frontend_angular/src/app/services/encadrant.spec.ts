import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Encadrant } from './encadrant';

describe('Encadrant', () => {
  let service: Encadrant;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Encadrant);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
