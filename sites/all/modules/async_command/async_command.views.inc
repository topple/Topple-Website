<?php

function async_command_views_data() {
  $data = array();
  $data['async_command'] = array(
    'table' => array(
      'group' => t('Async Command'),
      'help' => t('All issued commands of the Async Command module.'),
      'base' => array(
        'field' => 'id',
        'title' => t('Async Command'),
        'help' =>  t('All issued commands of the Async Command module.'),
      ),
    ),

    'id' => array(
      'title' => t('ID'),
      'help' => t('Command ID in the queue.'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),

    'app' => array(
      'title' => t('Application'),
      'help' => t('Application that issued the command.'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_string',
      ),
    ),

    'command' => array(
      'title' => t('Command'),
      'help' => t('Command issued.'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_string',
      ),
    ),

    'description' => array(
      'title' => t('Description'),
      'help' => t('Description of this command.'),
      'field' => array(
        'handler' => 'views_handler_field',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_string',
      ),
    ),

    // if you need field/sort/filter for uid, use the relationship first.
    'uid' => array(
      'title' => t('Uid'),
      'help' => t('User ID who issued the command.'),
      //'field' => array(
      //  'handler' => 'views_handler_field',
      //  'click sortable' => TRUE,
      //),
      //'sort' => array(
      //  'handler' => 'views_handler_sort',
      //),
      //'filter' => array(
      //  'handler' => 'views_handler_filter_user_name',
      //),
      'relationship' => array(
        'base' => 'users',
        'base field' => 'uid',
        'handler' => 'views_handler_relationship',
        'label' => t('User'),
      ),
    ),

    'eid' => array(
      'title' => t('Eid'),
      'help' => t('Entity ID associated with the command.'),
      // TODO: currently only support 'node' entity. later on should support any Drupal entity (taxonomy, etc).
      'relationship' => array(
        'base' => 'node',
        'base field' => 'nid',
        'handler' => 'views_handler_relationship',
        'label' => t('Entity'),
      ),
    ),

    'created' => array(
      'title' => t('Created'),
      'help' => t('The timestamp of this command was created.'),
      'field' => array(
        'handler' => 'views_handler_field_date',
        'click sortable' => TRUE,
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_date',
      ),
      'sort' => array(
        'handler' => 'views_handler_sort_date',
      ),
    ),

    'input' => array(
      'title' => t('Input'),
      'help' => t('Input of the command; usually a serialized string.'),
      'field' => array(
        'handler' => 'views_handler_field_serialized',
      ),
    ),

    'output' => array(
      'title' => t('Output'),
      'help' => t('Output of the command; usually a serialized string.'),
      'field' => array(
        'handler' => 'views_handler_field_serialized',
      ),
    ),

    'id1' => array(
      'title' => t('Parameter id1'),
      'help' => t('Parameter id1'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),

    'id2' => array(
      'title' => t('Parameter id2'),
      'help' => t('Parameter id2'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),

    'number1' => array(
      'title' => t('Parameter number1'),
      'help' => t('Parameter number1'),
      'field' => array(
        'handler' => 'views_handler_field_numeric',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),

    'number2' => array(
      'title' => t('Parameter number2'),
      'help' => t('Parameter number2'),
      'field' => array(
        'handler' => 'views_handler_field_numeric',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),

    'number3' => array(
      'title' => t('Parameter number3'),
      'help' => t('Parameter number3'),
      'field' => array(
        'handler' => 'views_handler_field_numeric',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),

    'number4' => array(
      'title' => t('Parameter number4'),
      'help' => t('Parameter number4'),
      'field' => array(
        'handler' => 'views_handler_field_numeric',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),

    'string1' => array(
      'title' => t('Parameter string1'),
      'help' => t('Parameter string1'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_string',
      ),
    ),

    'string2' => array(
      'title' => t('Parameter string2'),
      'help' => t('Parameter string2'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_string',
      ),
    ),

    /*'dependency' => array(
      'title' => t('Dependency ID'),
      'help' => t('Dependent command ID, which should be executed before this command.'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),*/

    // TODO: should show readable string rather than a 4-char code.
    'status' => array(
      'title' => t('Status'),
      'help' => t('Status of the command execution.'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_string',
      ),
    ),

    // TODO: should show form items (clickable button etc), rather than a 4-char string.
    'control' => array(
      'title' => t('Control'),
      'help' => t('Control instructions of the command execution.'),
      'field' => array(
        'handler' => 'views_handler_field',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_string',
      ),
    ),

    'message' => array(
      'title' => t('Message'),
      'help' => t('Output message of this command.'),
      'field' => array(
        'handler' => 'views_handler_field',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_string',
      ),
    ),

    'weight' => array(
      'title' => t('Weight'),
      'help' => t('Weight of this command; used for prioritize.'),
      'field' => array(
        'handler' => 'views_handler_field_numeric',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),

    'start' => array(
      'title' => t('Start timestamp'),
      'help' => t('The timestamp of this command starts to get executed.'),
      'field' => array(
        'handler' => 'views_handler_field_date',
        'click sortable' => TRUE,
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_date',
      ),
      'sort' => array(
        'handler' => 'views_handler_sort_date',
      ),
    ),

    'end' => array(
      'title' => t('End timestamp'),
      'help' => t('The timestamp of this command gets finished.'),
      'field' => array(
        'handler' => 'views_handler_field_date',
        'click sortable' => TRUE,
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_date',
      ),
      'sort' => array(
        'handler' => 'views_handler_sort_date',
      ),
    ),

    'checkpoint' => array(
      'title' => t('Checkpoint timestamp'),
      'help' => t('Checkpoint timestamp. Some long-time processing command might periodically set this timestamp'),
      'field' => array(
        'handler' => 'views_handler_field_date',
        'click sortable' => TRUE,
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_date',
      ),
      'sort' => array(
        'handler' => 'views_handler_sort_date',
      ),
    ),

    'progress' => array(
      'title' => t('Progress'),
      'help' => t('Progress of the execution, a 0 to 1 percentage number.'),
      'field' => array(
        'handler' => 'views_handler_field_numeric',
        'click sortable' => TRUE,
      ),
      'sort' => array(
        'handler' => 'views_handler_sort',
      ),
      'filter' => array(
        'handler' => 'views_handler_filter_numeric',
      ),
    ),

    // TODO/FIXME: add time spend computed field based on start and end.

  );
  return $data;
}