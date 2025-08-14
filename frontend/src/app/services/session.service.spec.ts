import { TestBed } from '@angular/core/testing';
import { SessionService } from './session.service';

describe('SessionService', () =>
{
  let service: SessionService;

  beforeEach(() =>
  {
    TestBed.configureTestingModule({
      providers: [SessionService],
    });
    service = TestBed.inject(SessionService);
  });

  afterEach(() =>
  {
    try
    {
      localStorage.removeItem('userId');
    }
    catch {}

    jasmine.clock().uninstall?.();
  });

  it('userId should return null when key is missing', () =>
  {
    spyOn(localStorage, 'getItem').and.returnValue(null);

    expect(service.userId).toBeNull();
  });

  it('userId should return a number when key exists', () =>
  {
    spyOn(localStorage, 'getItem').and.returnValue('42');

    expect(service.userId).toBe(42);
  });

  it('setUserId should store the id as a string under "userId"', () =>
  {
    const setSpy = spyOn(localStorage, 'setItem');
    service.setUserId(7);

    expect(setSpy).toHaveBeenCalledWith('userId', '7');
  });

  it('round-trip: setUserId then read userId', () =>
  {
    localStorage.setItem('userId', '13');

    expect(service.userId).toBe(13);

    service.setUserId(99);

    expect(localStorage.getItem('userId')).toBe('99');
    expect(service.userId).toBe(99);
  });

  it('userId returns NaN if localStorage contains a non-numeric string', () =>
  {
    spyOn(localStorage, 'getItem').and.returnValue('abc');
    expect(Number.isNaN(service.userId as any)).toBeTrue();
  });
});
