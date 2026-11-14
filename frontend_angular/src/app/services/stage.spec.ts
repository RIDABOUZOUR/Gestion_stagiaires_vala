import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Stage } from './stage';

describe('Stage', () => {
  let service: Stage;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Stage);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
