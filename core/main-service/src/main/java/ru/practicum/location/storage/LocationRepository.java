package ru.practicum.location.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.location.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {}
