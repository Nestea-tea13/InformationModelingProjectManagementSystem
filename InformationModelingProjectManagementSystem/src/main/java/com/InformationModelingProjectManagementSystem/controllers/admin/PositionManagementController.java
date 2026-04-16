package com.InformationModelingProjectManagementSystem.controllers.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.InformationModelingProjectManagementSystem.models.Person;
import com.InformationModelingProjectManagementSystem.models.Position;
import com.InformationModelingProjectManagementSystem.services.PeopleService;
import com.InformationModelingProjectManagementSystem.services.PositionService;
import com.InformationModelingProjectManagementSystem.util.PositionValidator;

@Controller
@RequestMapping("/adminpage/positions")
public class PositionManagementController {
    
    private final PositionService positionService;
    private final PeopleService peopleService;
    private final PositionValidator positionValidator;
    
    @Autowired
    public PositionManagementController(PositionService positionService, 
                                         PeopleService peopleService,
                                         PositionValidator positionValidator) {
        this.positionService = positionService;
        this.peopleService = peopleService;
        this.positionValidator = positionValidator;
    }
    
    @GetMapping("")
    public String positionManagementPage(Model model) {
        model.addAttribute("positions", positionService.findAll());
        model.addAttribute("users", peopleService.findByRole("ROLE_USER"));
        return "admin/positions/position-management";
    }
    
    @GetMapping("/create")
    public String createPositionForm(@ModelAttribute("position") Position position, Model model) {
        model.addAttribute("allPositions", positionService.findAll());
        return "admin/positions/position-form";
    }
    
    @PostMapping("/create")
    public String createPosition(@ModelAttribute("position") @Valid Position position,
                                 BindingResult bindingResult,
                                 @RequestParam(value = "subordinateIds", required = false) int[] subordinateIds,
                                 Model model) {
        
        positionValidator.validate(position, bindingResult);
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("allPositions", positionService.findAll());
            return "admin/positions/position-form";
        }
        
        // Добавляем подчиненных
        if (subordinateIds != null) {
            for (int subordinateId : subordinateIds) {
                Optional<Position> optionalSubordinate = positionService.findById(subordinateId);
                if (optionalSubordinate.isPresent()) {
                    position.addSubordinate(optionalSubordinate.get());
                }
            }
        }
        
        positionService.save(position);
        return "redirect:/adminpage/positions";
    }
    
    @GetMapping("/{id}/edit")
    public String editPositionForm(@PathVariable int id, Model model) {
        Optional<Position> optionalPosition = positionService.findById(id);
        
        if (!optionalPosition.isPresent()) {
            return "redirect:/adminpage/positions";
        }
        
        model.addAttribute("position", optionalPosition.get());
        model.addAttribute("allPositions", positionService.findAll());
        return "admin/positions/position-form";
    }
    
    @PostMapping("/{id}/edit")
    public String updatePosition(@PathVariable int id,
                                 @ModelAttribute("position") @Valid Position position,
                                 BindingResult bindingResult,
                                 @RequestParam(value = "subordinateIds", required = false) int[] subordinateIds,
                                 Model model) {
        
        position.setId(id);
        positionValidator.validate(position, bindingResult);
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("allPositions", positionService.findAll());
            return "admin/positions/position-form";
        }
        
        Optional<Position> optionalExistingPosition = positionService.findById(id);
        
        if (!optionalExistingPosition.isPresent()) {
            return "redirect:/adminpage/positions";
        }
        
        Position existingPosition = optionalExistingPosition.get();
        
        // Очищаем старые связи с подчиненными
        List<Position> oldSubordinates = new ArrayList<>(existingPosition.getCanAssignTo());
        for (Position oldSubordinate : oldSubordinates) {
            existingPosition.removeSubordinate(oldSubordinate);
        }
        
        // Добавляем новые связи
        if (subordinateIds != null) {
            for (int subordinateId : subordinateIds) {
                Optional<Position> optionalSubordinate = positionService.findById(subordinateId);
                if (optionalSubordinate.isPresent()) {
                    existingPosition.addSubordinate(optionalSubordinate.get());
                }
            }
        }
        
        existingPosition.setName(position.getName());
        
        positionService.save(existingPosition);
        return "redirect:/adminpage/positions";
    }
    
    @PostMapping("/{id}/delete")
    public String deletePosition(@PathVariable int id, Model model) {
        Optional<Position> optionalPosition = positionService.findById(id);
        
        if (optionalPosition.isPresent()) {
            Position position = optionalPosition.get();
            List<Person> usersWithPosition = peopleService.findByPosition(position);
            
            if (!usersWithPosition.isEmpty()) {
                model.addAttribute("error", "Невозможно удалить должность, так как есть сотрудники с этой должностью");
                model.addAttribute("positions", positionService.findAll());
                model.addAttribute("users", peopleService.findByRole("ROLE_USER"));
                return "admin/positions/position-management";
            }
        }
        
        positionService.deleteById(id);
        return "redirect:/adminpage/positions";
    }
    
    @PostMapping("/assign-user")
    public String assignPositionToUser(@RequestParam int userId, @RequestParam int positionId) {
        Optional<Position> optionalPosition = positionService.findById(positionId);
        
        if (optionalPosition.isPresent()) {
            peopleService.updateUserPosition(userId, optionalPosition.get());
        }
        
        return "redirect:/adminpage/positions";
    }
}